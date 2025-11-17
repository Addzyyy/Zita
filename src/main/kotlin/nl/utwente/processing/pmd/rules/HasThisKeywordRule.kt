package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that checks whether the code uses the 'this' keyword.
 * If the 'this' keyword is found, a violation is reported.
 */
class HasThisKeywordRule : AbstractJavaRule() {

    private var referenceNode: Node? = null

    override fun visit(node: ASTPrimaryExpression?, data: Any?): Any? {
        val prefix = node?.getFirstChildOfType(ASTPrimaryPrefix::class.java)
        if (prefix?.usesThisModifier() == true && referenceNode == null) {
            referenceNode = node
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        val node = referenceNode
        if (ctx != null && node != null) {
            addViolationWithMessage(ctx, node, message, node.beginLine, node.endLine)
        }
        super.end(ctx)
    }
}