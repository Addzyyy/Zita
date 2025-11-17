package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that checks if user-defined classes have explicit constructors.
 * Fails if:
 * - No user-defined classes are found, OR
 * - User-defined classes exist but none have constructors
 *
 * Passes if:
 * - At least one user-defined class has an explicit constructor
 */
class HasClassWithConstructorRule : AbstractJavaRule() {
    private var compilationUnit: ASTCompilationUnit? = null
    private var hasUserDefinedClass = false
    private var hasConstructor = false

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any?): Any? {
        // Mark that we found a user-defined class
        hasUserDefinedClass = true

        // Check if this class has a constructor
        val constructors = node.findDescendantsOfType(ASTConstructorDeclaration::class.java)
        if (constructors.isNotEmpty()) {
            hasConstructor = true
        }

        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx != null && compilationUnit != null) {
            if (!hasUserDefinedClass) {
                // No classes found at all
                addViolationWithMessage(
                    ctx,
                    compilationUnit,
                    "No user-defined classes were found. Try adding a simple class with an explicit constructor to initialize state and keep your code organized. if you need any help udnersatanding this rule your TA is there to help",
                    0,
                    0
                )

            } else if (!hasConstructor) {
                // Classes exist but none have constructors
                addViolationWithMessage(
                    ctx,
                    compilationUnit,
                    "User-defined classes exist but none have explicit constructors. Try adding a simple class with an explicit constructor to initialize state and keep your code organized. if you need any help udnersatanding this rule your TA is there to help",
                    0,
                    0
                )

            }
            // If hasConstructor is true, no violation (pass)
        }
        super.end(ctx)
    }
}