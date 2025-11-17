package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.ASTConditionalAndExpression
import net.sourceforge.pmd.lang.java.ast.ASTConditionalOrExpression
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit

/**
 * Rule that flags if the program does NOT contain any boolean operator (&& or ||).
 */
class HasNoBooleanOperatorRule : AbstractJavaRule() {
    private var hasBooleanOperator = false
    private var firstNode: Node? = null
    private var anyNode: Node? = null

    override fun visit(node: ASTConditionalAndExpression, data: Any?): Any? {
        if (firstNode == null) firstNode = node
        hasBooleanOperator = true
        return super.visit(node, data)
    }

    override fun visit(node: ASTConditionalOrExpression, data: Any?): Any? {
        if (firstNode == null) firstNode = node
        hasBooleanOperator = true
        return super.visit(node, data)
    }

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        if (anyNode == null) anyNode = node
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (!hasBooleanOperator) {
            val nodeToFlag = firstNode ?: anyNode
            if (nodeToFlag != null) {
                addViolationWithMessage(
                    ctx,
                    nodeToFlag,
                    message,
                    0,
                    0
                )
            }
        }
        super.end(ctx)
    }
}
