package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that flags if the program does NOT contain any 'else' statement.
 */
class HasNoElseStatementRule : AbstractJavaRule() {
    private var hasElse = false
    private var compilationUnit: ASTCompilationUnit? = null

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTIfStatement, data: Any?): Any? {
        if (node.hasElse()) {
            hasElse = true
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (!hasElse && ctx != null && compilationUnit != null) {
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

private fun ASTIfStatement.hasElse(): Boolean {
    return this.elseBranch != null
}