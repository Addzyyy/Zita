package nl.utwente.processing.pmd.utils

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.symboltable.ClassScope
import net.sourceforge.pmd.lang.java.symboltable.MethodNameDeclaration
import net.sourceforge.pmd.lang.java.symboltable.SourceFileScope
import net.sourceforge.pmd.lang.symboltable.Scope
import net.sourceforge.pmd.lang.symboltable.ScopedNode
import java.util.stream.Collectors

/**
 * Property on scopes to check if the scope is directly under the top class scope.
 */
val Scope.isPartOfTopClassScope : Boolean
    get() {
        var current = this;
        while (current !is ClassScope) {
            current = current.parent;
        }
        return current.parent is SourceFileScope
    }

fun <T> Scope.findDeclaration(node: ScopedNode, type: Class<T>): T? {
    val matches = this.declarations
            .filterKeys { type.isAssignableFrom(type) }
            .filter { it.value.any { usage -> usage.location == node } }
    if (matches.isEmpty()) {
        if (this.parent == null) return null
        else return this.parent.findDeclaration(node, type)
    } else {
        return type.cast(matches.keys.first())
    }
}

/**
 * Extension method which returns a map of callers mapped to callee's.
 */
fun ClassScope.callStack(): Map<ASTMethodDeclaration, Set<ASTMethodDeclaration>> {
    val methods = HashMap(this.methodDeclarations.mapKeysToNodes())
    for (classDecl in this.classDeclarations.keys) {
        val node = classDecl.accessNodeParent as? ASTClassOrInterfaceDeclaration
        val scope = node?.scope as? ClassScope
        scope?.let { s -> methods.putAll(s.methodDeclarations.mapKeysToNodes()) }
    }
    val result = HashMap<ASTMethodDeclaration, MutableSet<ASTMethodDeclaration>>()
    methods.let {
        for ((callee, occurences) in methods) {
            occurences?.let {
                occurences.stream().
                        map { o -> o.location.getFirstParentOfType(ASTMethodDeclaration::class.java) }.
                        forEach { caller -> result.putIfAbsent(caller, HashSet(setOf(callee)))?.add(callee) }
            }
        }
    }
    return result
}

/**
 * Extension method which returns all methods that are in the call stacks of the specified methods.
 * @param methods The methods to get the call stacks for.
 */
fun ClassScope.callStack(vararg methods: MethodNameDeclaration) : Set<ASTMethodDeclaration> {
    val totalStack = this.callStack();
    val stack = HashSet<ASTMethodDeclaration>()
    for (method in methods) {
        val methodDecl = method.methodNameDeclaratorNode.getFirstParentOfType(ASTMethodDeclaration::class.java)
        stack.addAll(totalStack[methodDecl] ?: emptySet())
        stack.add(methodDecl)
    }
    var size: Int
    do {
        size = stack.size
        HashSet(stack).forEach { m -> stack.addAll(this.callStack()[m] ?: emptySet()) }
    } while (stack.size != size)
    return stack
}

/**
 * Extension method which returns the methods that are uniquely in the call stacks of the specified methods.
 * All methods that are also called by methods outside of the call stacks are not returned in the final set.
 * @param method The methods to get the locally unique call stacks for.
 */
fun ClassScope.uniqueCallStack(vararg methods: MethodNameDeclaration) : Set<ASTMethodDeclaration> {
    val stack = this.callStack()
    val uniqueStack = HashSet(this.callStack(*methods))
    return uniqueStack.stream().filter {
        m -> uniqueStack.containsAll(stack.keysByValue(m))
    }.collect(Collectors.toSet())
}

/**
 * Extension method which finds a method defined in the class scope, or returns null when not defined.
 * @param signature The signature of the method to find in the scope.
 */
fun ClassScope.findMethod(signature: String) : MethodNameDeclaration? {
    return this.methodDeclarations.keys.stream().
            filter { d -> signature == d.name + d.parameterDisplaySignature }.
            findFirst().orElse(null)
}

fun ClassScope.findMethods(signatures: Collection<String>) : Array<MethodNameDeclaration> {
    return this.methodDeclarations.keys.stream()
            .filter { d -> signatures.contains(d.name + d.parameterDisplaySignature) }
            .collect(Collectors.toSet()).toTypedArray()
}