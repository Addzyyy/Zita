package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTImportDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that flags if the program contains any import statement.
 */
class HasImportStatementRule : AbstractJavaRule() {
    private var hasImport = false
    private var firstNode: ASTCompilationUnit? = null

    override fun visit(node: ASTImportDeclaration, data: Any?): Any? {
        hasImport = true
        return super.visit(node, data)
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        if (firstNode == null) firstNode = node
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (hasImport && firstNode != null) {
            addViolationWithMessage(
                ctx,
                firstNode,
                message,
                0,
                0
            )
        }
        super.end(ctx)
    }
}
