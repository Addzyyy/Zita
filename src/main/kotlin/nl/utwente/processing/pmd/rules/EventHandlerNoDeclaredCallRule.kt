package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.ASTName
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmd.symbols.ProcessingApplet

/**
 * Rule that flags if none of the event handler methods call any declared methods in the program.
 * If at least one event handler calls a declared method, no violation is reported for any event handler.
 */
class EventHandlerNoDeclaredCallRule : AbstractJavaRule() {
    private var foundDeclaredCall = false
    private val eventHandlersToFlag = mutableListOf<ASTMethodDeclaration>()

    override fun visit(node: ASTMethodDeclaration, data: Any?): Any? {
        val methodName = node.name
        if (ProcessingApplet.EVENT_METHOD_SIGNATURES.any { it.startsWith(methodName) }) {
            // Collect all declared method names in the entire file (compilation unit)
            val cu = node.getFirstParentOfType(net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit::class.java)
            val declaredNames = cu?.findDescendantsOfType(ASTMethodDeclaration::class.java)
                ?.map { it.name }?.toSet() ?: emptySet()
            // Find all method calls
            val calls = node.findDescendantsOfType(ASTPrimaryExpression::class.java)
            val callsDeclared = calls.any { call ->
                val name = getMethodCallName(call)
                name != null && declaredNames.contains(name)
            }
            if (callsDeclared) {
                foundDeclaredCall = true
            } else {
                eventHandlersToFlag.add(node)
            }
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (!foundDeclaredCall) {
            for (node in eventHandlersToFlag) {
                addViolationWithMessage(
                    ctx,
                    node,
                    message,
                    0,
                    0
                )
            }
        }
        super.end(ctx)
    }

    private fun getMethodCallName(node: ASTPrimaryExpression): String? {
        val prefix = node.getFirstChildOfType(net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix::class.java)
        val nameNode = prefix?.getFirstChildOfType(ASTName::class.java)
        return nameNode?.image?.substringBefore('.')
    }
}
