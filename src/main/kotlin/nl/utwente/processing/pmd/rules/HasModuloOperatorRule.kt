package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.ASTMultiplicativeExpression
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that flags the use of the modulo operator ('%') anywhere in the code, including inner classes.
 * Useful for detecting usage of modulo in Processing assignments.
 */
class HasModuloOperatorRule : AbstractJavaRule() {
    private var referenceNode: Node? = null
    private var firstSeenNode: Node? = null

    override fun visit(node: ASTMultiplicativeExpression, data: Any?): Any? {
        if (firstSeenNode == null) firstSeenNode = node
        if (node.image == "%" && referenceNode == null) {
            referenceNode = node
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (referenceNode == null && firstSeenNode != null) {
            addViolationWithMessage(
                ctx,
                firstSeenNode,
                message,
                0,
                0
            )
        }
        super.end(ctx)
    }
}
