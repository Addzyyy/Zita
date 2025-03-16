package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.lang.java.symboltable.ClassScope
import net.sourceforge.pmd.lang.java.symboltable.MethodScope
import net.sourceforge.pmd.lang.java.symboltable.VariableNameDeclaration
import net.sourceforge.pmd.lang.symboltable.NameOccurrence
import kotlin.collections.ArrayList
import kotlin.math.exp
import kotlin.math.expm1

/**
 * Class that implements the OutOfScopeStateChange rule.
 * <p>
 *     This is rule defines that global variables, variables
 *     that are defined within the main class of the Processing
 *     application, and variables that are out of the scope
 *     of the current class, should not have their state changed
 *     from anywhere except the class that defined them. Boolean
 *     operations are allowed however.
 * </p>
 */
class OutOfScopeStateChangeRule: AbstractJavaRule() {

    private var classDeclarations  = ArrayList<String>()
    private var methodDeclarations = ArrayList<String>()
    private var globalDeclarations = ArrayList<String>()
    private var currentClassName = ""
    private var currentMethodName = ""

    /* Constructor Handler */
    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any): Any? {
        if (!node.isNested) {
           globalDeclarations = extractImages((node.scope as? ClassScope)?.variableDeclarations!!)
        } else {
            val constructor = node.getFirstDescendantOfType(ASTConstructorDeclaration::class.java)
            if (constructor != null) {
                val nodeScope = extractImages((node.scope as? ClassScope)?.variableDeclarations!!)
                val constScope = extractImages((constructor.scope as? MethodScope)?.variableDeclarations!!)
                for (expression in constructor.findDescendantsOfType(ASTStatementExpression::class.java)) {
                    if (expression.hasDescendantOfType(ASTAssignmentOperator::class.java) || expression.hasDescendantOfType(ASTPostfixExpression::class.java)) {
                        /* It's this long since it need to account for a possible 'this' being in front of variable */
                        var varName = if (expression.getFirstDescendantOfType(ASTPrimaryPrefix::class.java)
                                        .hasDescendantOfType(ASTName::class.java))
                            expression.getFirstDescendantOfType(ASTName::class.java).image else
                            expression.getFirstDescendantOfType(ASTPrimarySuffix::class.java).image
                        varName = if (varName.contains(".")) varName.split(".")[0] else varName
                        if (!nodeScope.contains(varName) && !constScope.contains(varName) && globalDeclarations.contains(varName)) {
                            this.addViolationWithMessage(data, expression, message,
                                    arrayOf(varName, "Constructor"))
                        }
                    }
                }
            }
        }
        return super.visit(node, data)
    }

    /* Method Handler */
    override fun visit(node: ASTMethodDeclaration, data: Any): Any? {
        val currentClass = node.getFirstParentOfType(ASTClassOrInterfaceDeclaration::class.java)
        if (currentClass.simpleName != currentClassName) {
            currentClassName = currentClass.simpleName
            classDeclarations = extractImages((currentClass.scope as? ClassScope)?.variableDeclarations!!)
        }
        if (node.name != currentMethodName) {
            currentMethodName = node.name
            methodDeclarations = extractImages((node.scope as? MethodScope)?.variableDeclarations!!)
        }
        for (expression in node.findDescendantsOfType(ASTStatementExpression::class.java)) {
            if (expression.hasDescendantOfType(ASTAssignmentOperator::class.java) || expression.hasDescendantOfType(ASTPostfixExpression::class.java)) {
                /* It's this long since it need to account for a possible 'this' being in front of variable */
                var varName = if (expression.getFirstDescendantOfType(ASTPrimaryPrefix::class.java)
                                .hasDescendantOfType(ASTName::class.java))
                                    expression.getFirstDescendantOfType(ASTName::class.java).image else
                                        expression.getFirstDescendantOfType(ASTPrimarySuffix::class.java).image
                varName = if (varName.contains(".")) varName.split(".")[0] else varName
                if (!classDeclarations.contains(varName) && !methodDeclarations.contains(varName) && globalDeclarations.contains(varName)) {
                    this.addViolationWithMessage(data, expression, message,
                            arrayOf(varName, currentMethodName))
                }
            }
        }
        return super.visit(node, data)
    }

    /* Helper Methods */

    /**
     * Function for extracting the variable names for all variables withing a ClassScope's scope.
     *
     * @param variableDeclarations Map of variables name declarations and list of occurrences
     * @return A list of all variable names withing the ClassScope
     */
    private fun extractImages(variableDeclarations: Map<VariableNameDeclaration, List<NameOccurrence>>): ArrayList<String> {
        val res = ArrayList<String>()
        for (variableName in variableDeclarations.keys) {
            res.add(variableName.image)
        }
        return res
    }
}