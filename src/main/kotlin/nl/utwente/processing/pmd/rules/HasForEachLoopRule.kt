package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTForStatement
import net.sourceforge.pmd.lang.java.ast.ASTExpression
import net.sourceforge.pmd.lang.java.ast.ASTForInit
import net.sourceforge.pmd.lang.java.ast.ASTForUpdate
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule to detect use of enhanced for-each loops like: for (Type item : collection)
 */
class HasForEachLoopRule : AbstractJavaRule() {

    private var firstForEachNode: ASTForStatement? = null

    override fun visit(node: ASTForStatement, data: Any?): Any? {
        val hasExpression = node.getFirstDescendantOfType(ASTExpression::class.java) != null
        val hasNoInit = node.getFirstChildOfType(ASTForInit::class.java) == null
        val hasNoUpdate = node.getFirstChildOfType(ASTForUpdate::class.java) == null

        if (hasExpression && hasNoInit && hasNoUpdate && firstForEachNode == null) {
            firstForEachNode = node
        }

        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx != null && firstForEachNode != null) {
            addViolationWithMessage(
                ctx,
                firstForEachNode,
                message,
                firstForEachNode!!.beginLine,
                firstForEachNode!!.endLine
            )
        }
        super.end(ctx)
    }
}