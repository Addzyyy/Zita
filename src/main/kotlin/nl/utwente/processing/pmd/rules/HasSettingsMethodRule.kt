package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that checks whether there is a method named "settings" defined in the code.
 * If such a method is found, a violation is reported.
 */
class HasSettingsMethodRule : AbstractJavaRule() {
    private var compilationUnit: ASTCompilationUnit? = null
    private var settingsMethod: ASTMethodDeclaration? = null

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTMethodDeclaration, data: Any?): Any? {
        if (node.name == "settings") {
            settingsMethod = node
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx != null && settingsMethod != null) {
            addViolationWithMessage(
                ctx,
                settingsMethod,
                message,
                0,
                0
            )
        }
        super.end(ctx)
    }
}