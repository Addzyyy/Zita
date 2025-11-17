package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTMultiplicativeExpression
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

class HasModuloOperatorRule : AbstractJavaRule() {
    private var hasModulo = false
    private var compilationUnit: ASTCompilationUnit? = null

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTMultiplicativeExpression, data: Any?): Any? {
        if (node.image == "%") {
            hasModulo = true
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (!hasModulo && ctx != null && compilationUnit != null) {
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