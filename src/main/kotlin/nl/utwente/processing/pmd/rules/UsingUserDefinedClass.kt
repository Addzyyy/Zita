package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that checks if user-defined inner classes are actually used in the code.
 * Reports violations for inner classes that:
 * - Have a constructor but are never instantiated
 * - Have no constructor and are never instantiated
 */
class UsingUserDefinedClass: AbstractJavaRule() {

    private var outerClassName: String? = null
    private val definedClasses = mutableSetOf<String>()
    private val classesWithConstructors = mutableSetOf<String>()
    private val classesUsedWithNew = mutableSetOf<String>()
    private val classNodeMap = mutableMapOf<String, ASTClassOrInterfaceDeclaration>()
    private var compilationUnit: ASTCompilationUnit? = null

    override fun visit(node: ASTCompilationUnit?, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTClassOrInterfaceDeclaration?, data: Any?): Any? {
        if (node == null || node.isInterface) return data

        if (outerClassName == null) {
            // First class = outer Processing wrapper
            outerClassName = node.image
        } else {
            // This is an inner class
            val className = node.image
            definedClasses.add(className)
            classNodeMap[className] = node  // Store the actual class node for violation reporting

            val hasConstructor = node
                .findDescendantsOfType(ASTConstructorDeclaration::class.java)
                .any { it.image == null || it.image == className }

            if (hasConstructor) {
                classesWithConstructors.add(className)
            }
        }

        return super.visit(node, data)
    }

    override fun visit(node: ASTAllocationExpression?, data: Any?): Any? {
        val type = node?.getFirstChildOfType(ASTClassOrInterfaceType::class.java)
        val typeName = type?.image
        if (typeName != null) {
            classesUsedWithNew.add(typeName)
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx == null) return

        // Check if there are any inner classes defined
        if (definedClasses.isEmpty()) {
            // No inner classes found at all
            if (compilationUnit != null) {
                addViolationWithMessage(
                    ctx,
                    compilationUnit,
                    "No user-defined inner classes found. Your submission should include at least one custom class.",
                    0, 0
                )
            }
            super.end(ctx)
            return
        }

        // Check each inner class for proper usage
        for (className in definedClasses) {
            val hasConstructor = className in classesWithConstructors
            val wasUsed = className in classesUsedWithNew
            val classNode = classNodeMap[className]

            if (classNode != null) {
                if (hasConstructor && !wasUsed) {
                    // Class has constructor but is never instantiated
                    addViolationWithMessage(
                        ctx,
                        classNode,
                        "Class '$className' has a constructor but is never instantiated. Try creating objects from this class (for example, `new $className(...)`) to use its behavior and keep your program modular. if you need any help understanding this rule your TA is there to help",
                        0, 0
                    )
                } else if (!hasConstructor && !wasUsed) {
                    // Class has no constructor and is never instantiated
                    addViolationWithMessage(
                        ctx,
                        classNode,
                        "Class '$className' has no explicit constructor and is never instantiated. Try adding a simple constructor to initialize state and create objects using `new $className(...)` to keep your code organized and testable. if you need any help understanding this rule your TA is there to help",
                        0, 0
                    )
                } else if (!hasConstructor && wasUsed) {
                    // Class is used but has no explicit constructor (using default constructor)
                    addViolationWithMessage(
                        ctx,
                        classNode,
                        "Class '$className' is instantiated but has no explicit constructor. Try adding a constructor to initialize the class's state and improve clarity. if you need any help understanding this rule your TA is there to help",
                        0, 0
                    )
                }
                // If hasConstructor && wasUsed: PASS - no violation
            }

        }

        super.end(ctx)
    }
}