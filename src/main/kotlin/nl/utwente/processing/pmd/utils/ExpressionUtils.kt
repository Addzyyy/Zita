package nl.utwente.processing.pmd.utils

import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.*
import nl.utwente.processing.pmd.symbols.ProcessingAppletMethod
import java.util.*

val ASTPrimaryExpression.isMethodCall : Boolean
    get() {
        return this.findChildrenOfType(ASTPrimarySuffix::class.java).stream().filter { s -> s.isArguments }.count() > 0
    }

fun ASTPrimaryExpression.isLiteral() : Boolean {
    val literal = this.getFirstChildOfType(ASTPrimaryPrefix::class.java)
            ?.getFirstChildOfType(ASTLiteral::class.java)
    return literal != null
}

fun ASTPrimaryExpression.isLambda() : Boolean {
    val lambda = this.getFirstChildOfType(ASTPrimaryPrefix::class.java)
            ?.getFirstChildOfType(ASTLambdaExpression::class.java)
    return lambda != null
}

fun ASTPrimaryExpression.isLowest() : Boolean {
    return this.getFirstDescendantOfType(ASTPrimaryExpression::class.java) == null
}

fun Node.similar(other: Node) : Boolean {
    return this.numChildren == other.numChildren
            && this.image == other.image
            && this.children().zip(other.children()).all { it.first.similar(it.second) }
}

fun ASTPrimaryExpression.isInDirectArrayDereference() : Boolean {
    if (this.parent == null) return false
    val pp = this.parent.parent
    if (pp !== null && pp is ASTPrimarySuffix)
        return pp.isArrayDereference
    return false
}

fun ASTPrimaryExpression.dereferencedArray() : ASTPrimaryPrefix? {
    if (!this.isInDirectArrayDereference()) return null
    return this.parent.parent.parent.getFirstChildOfType(ASTPrimaryPrefix::class.java)
}

fun ASTPrimaryExpression.isInManipulation() : Boolean {
    if (this.parent == null) return false
    return when (this.parent) {
        is ASTPostfixExpression -> true
        is ASTPreIncrementExpression -> true
        is ASTPreDecrementExpression -> true
        is ASTStatementExpression ->
            (this.parent as ASTStatementExpression).getFirstChildOfType(ASTPrimaryExpression::class.java) == this
            && (this.parent as ASTStatementExpression).getFirstChildOfType(ASTAssignmentOperator::class.java) != null
        else -> false
    }
}

fun ASTPrimaryExpression.isInConstantManipulation() : Boolean {
    return this.isInManipulation()
        && this.parent.findDescendantsOfType(ASTPrimaryExpression::class.java)
            .all { it.isLiteral() || it.similar(this) }
}

fun Node.deepImage() : String {
    val sb = StringBuilder()
    this.deepImage(sb)
    return sb.toString()
}

fun Node.deepImage(sb: StringBuilder) {
    if (this.image != null) {
        sb.append(image)
    } else {
        for (child in this.children()) child.deepImage(sb)
    }
}

fun TypeNode.isNumeral() : Boolean {
    return this.typeDefinition?.type in listOf(
            Byte::class.java, Short::class.java,
            Int::class.java, Long::class.java,
            Float::class.java, Double::class.java)
}

fun Node.getContainingClass() : ASTClassOrInterfaceDeclaration? {
    return this.getFirstParentOfType(ASTClassOrInterfaceDeclaration::class.java)
}

fun ASTPrimaryExpression.hasLiteralArguments(method: ProcessingAppletMethod) : Boolean {
    val argumentNode = this.findChildrenOfType(ASTPrimarySuffix::class.java).stream()
            .filter { s -> s.isArguments }.findFirst().orElse(null) ?: return false
    val argumentList = argumentNode.getFirstDescendantOfType(ASTArgumentList::class.java) ?: return false
    return (0..argumentList.getNumChildren()-1)
            .filter { method.parameters[it].pixels }
            .map {
                argumentList.getChild(it)?.getFirstChildOfType(ASTPrimaryExpression::class.java)
            }
            .any { it != null && it.isLiteral() };
}

fun ASTPrimaryExpression.matchesBuiltinInstanceCall(method: ProcessingAppletMethod): Boolean {
    var result = false;
    check@ for (node in this.children()) {
        when (node) {
            // The primary prefix is usually the object reference and the method name, object.method
            is ASTPrimaryPrefix -> {
                if (node.usesThisModifier() || node.usesSuperModifier()) {
                    // If it uses this. or super., it is not a call to an instance method made from outside the class
                    break@check
                } else if(node.getFirstChildOfType(ASTName::class.java)?.image?.endsWith("." + method.name) != true) {
                    // If the image doesn't end with .methodName, this is not a call to this method
                    break@check
                }
                // Don't do the nameDeclaration check here, because it will also find the instance variable declaration
            }
            is ASTPrimarySuffix -> {
                // The first prefix needs to be arguments and have the correct amount of them
                // We don't do a type check, because PMD doesn't really have enough information to do
                // that fully reliably
                result = node.isArguments && node.argumentCount == method.parameters.size;
                break@check
            }
        }
    }
    return result
}

fun ASTPrimaryExpression.matchesBuiltinInstanceCall(methods: Collection<ProcessingAppletMethod>): ProcessingAppletMethod? {
    return methods.firstOrNull { this.matchesBuiltinInstanceCall(it) }
}

fun ASTPrimaryExpression.matches(method: ProcessingAppletMethod) : Boolean {
    var result = false;
    check@ for (node in this.children()) {
        when (node) {
            is ASTPrimaryPrefix -> {
                if (node.usesThisModifier() || node.usesSuperModifier()) {
                    if (!this.scope.isPartOfTopClassScope) {
                        break@check
                    }
                } else if(node.getFirstChildOfType(ASTName::class.java)?.image != method.name) {
                    break@check
                } else if(node.getFirstChildOfType(ASTName::class.java)?.nameDeclaration != null) {
                    break@check
                }
            }
            is ASTPrimarySuffix -> {
                result = node.isArguments && node.argumentCount == method.parameters.size;
                break@check
            }
        }
    }
    return result
}

fun ASTPrimaryExpression.matches(variable: String) : Boolean {
    var result = false
    check@ for (i in 0..this.getNumChildren()-1) {
        val node = this.getChild(i)
        when (node) {
            is ASTPrimaryPrefix -> {
                if (node.usesThisModifier() || node.usesSuperModifier()) {
                    if (!this.scope.isPartOfTopClassScope) {
                        break@check
                    }
                } else if(node.getFirstChildOfType(ASTName::class.java) == null) {
                    break@check
                } else if(node.getFirstChildOfType(ASTName::class.java).image != variable) {
                    break@check
                } else if(node.getFirstChildOfType(ASTName::class.java).nameDeclaration != null) {
                    break@check
                } else {
                    result = true
                }
            }
        }
    }
    return result
}

fun ASTPrimaryExpression.matches(vararg methods: ProcessingAppletMethod) : ProcessingAppletMethod? {
    return Arrays.stream(methods).filter { m -> this.matches(m) }.findFirst().orElse(null)
}

fun ASTPrimaryExpression.matches(vararg variables: String) : String? {
    return Arrays.stream(variables).filter { m -> this.matches(m) }.findFirst().orElse(null)
}