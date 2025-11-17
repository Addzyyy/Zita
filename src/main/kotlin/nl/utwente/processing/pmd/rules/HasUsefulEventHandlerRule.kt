package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmd.symbols.ProcessingApplet

/**
 * Rule that checks whether event handler methods (like mousePressed, keyPressed) contain useful code.
 * An event handler is considered useful if it contains control flow statements (if, for, while)
 * or calls to other user-defined methods.
 * If event handlers are found that do not meet these criteria, a violation is reported.
 * Additionally, if no event handlers are found at all, a violation is also reported.
 */
class HasUsefulEventHandlerRule : AbstractJavaRule() {
    private var foundComplexity = false
    private val eventHandlersToFlag = mutableListOf<ASTMethodDeclaration>()
    private var declaredMethodNames: Set<String> = emptySet()
    private var compilationUnit: ASTCompilationUnit? = null
    private var hasAnyEventHandlers = false  // Track if we found any event handlers

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        declaredMethodNames = node.findDescendantsOfType(ASTMethodDeclaration::class.java)
            .map { it.name }
            .toSet()
        return super.visit(node, data)
    }

    override fun visit(node: ASTMethodDeclaration, data: Any?): Any? {
        val methodName = node.name

        if (ProcessingApplet.EVENT_METHOD_SIGNATURES.any { it.startsWith(methodName) }) {
            hasAnyEventHandlers = true  // Found an event handler

            val hasComplexity = hasControlFlow(node) || callsDeclaredMethod(node)

            if (hasComplexity) {
                foundComplexity = true
            } else {
                eventHandlersToFlag.add(node)
            }
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        // Report violation if:
        // 1. No event handlers exist at all, OR
        // 2. Event handlers exist but none have complexity
        if (ctx != null) {
            if (!hasAnyEventHandlers && compilationUnit != null) {
                // No event handlers found
                addViolationWithMessage(
                    ctx,
                    compilationUnit,
                    message,
                    0,
                    0
                )

            } else if (!foundComplexity && eventHandlersToFlag.isNotEmpty()) {
                // Event handlers exist but are too simple
                val node = eventHandlersToFlag.first()
                addViolationWithMessage(
                    ctx,
                    node,
                    message,0
                    ,
                    0
                )

            }
        }
        super.end(ctx)
    }

    private fun hasControlFlow(node: ASTMethodDeclaration): Boolean {
        val hasFor = node.findDescendantsOfType(ASTForStatement::class.java).isNotEmpty()
        val hasIf = node.findDescendantsOfType(ASTIfStatement::class.java).isNotEmpty()
        val hasWhile = node.findDescendantsOfType(ASTWhileStatement::class.java).isNotEmpty()
        return hasFor || hasIf || hasWhile
    }

    private fun callsDeclaredMethod(node: ASTMethodDeclaration): Boolean {
        val calls = node.findDescendantsOfType(ASTPrimaryExpression::class.java)
        return calls.any { call ->
            val name = getMethodCallName(call)
            // Check for instance method calls (e.g., hero.stop()) OR direct user-defined method calls
            name != null && (name.contains('.') || declaredMethodNames.contains(name))
        }
    }

    private fun getMethodCallName(node: ASTPrimaryExpression): String? {
        val prefix = node.getFirstChildOfType(ASTPrimaryPrefix::class.java)
        val nameNode = prefix?.getFirstChildOfType(ASTName::class.java)
        return nameNode?.image
    }
}