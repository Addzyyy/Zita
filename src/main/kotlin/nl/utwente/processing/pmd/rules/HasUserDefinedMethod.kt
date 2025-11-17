package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmd.symbols.ProcessingApplet

/**
 * Rule that checks if there are user-defined methods (excluding Processing lifecycle methods).
 * Looks for methods in both the main class and inner classes.
 */
class HasUserDefinedMethod : AbstractJavaRule() {
    private var compilationUnit: ASTCompilationUnit? = null
    private var hasUserDefinedMethod = false

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTMethodDeclaration, data: Any?): Any? {
        val methodName = node.name

        // Get event method names (without parameters)
        val eventMethodNames = ProcessingApplet.EVENT_METHOD_SIGNATURES
            .map { it.substringBefore("(") }
            .toSet()

        // Check if this is a user-defined method (not draw, setup, or event handlers)
        if (methodName != "draw" &&
            methodName != "setup" &&
            methodName !in eventMethodNames
        ) {
            hasUserDefinedMethod = true
        }

        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (!hasUserDefinedMethod && ctx != null && compilationUnit != null) {
            addViolationWithMessage(
                ctx,
                compilationUnit,
               message,
                0,
                0
            )
        }
        super.end(ctx)
    }
}