package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTForStatement
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmd.symbols.ProcessingApplet

/**
 * Rule that flags if none of the event handler methods contain a for loop or if statement.
 * If at least one event handler contains a for loop or if statement, no violation is reported for any event handler.
 */
class EventHandlerNoControlFlowRule : AbstractJavaRule() {
    private var foundControlFlow = false
    private val eventHandlersToFlag = mutableListOf<ASTMethodDeclaration>()

    override fun visit(node: ASTMethodDeclaration, data: Any?): Any? {
        val methodName = node.name
        if (ProcessingApplet.EVENT_METHOD_SIGNATURES.any { it.startsWith(methodName) }) {
            val hasFor = node.findDescendantsOfType(ASTForStatement::class.java).isNotEmpty()
            val hasIf = node.findDescendantsOfType(ASTIfStatement::class.java).isNotEmpty()
            val hasWhile = node.findDescendantsOfType(net.sourceforge.pmd.lang.java.ast.ASTWhileStatement::class.java).isNotEmpty()
            if (hasFor || hasIf || hasWhile) {
                foundControlFlow = true
            } else {
                eventHandlersToFlag.add(node)
            }
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (!foundControlFlow && eventHandlersToFlag.isNotEmpty()) {
            val node = eventHandlersToFlag.first()
            addViolationWithMessage(
                ctx,
                node,
                message,
                0,
                0
            )
        }
        super.end(ctx)
    }
}
