package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that flags if the program uses ArrayList.
 * Students should use traditional arrays instead.
 */
class HasArrayListRule : AbstractJavaRule() {
    private var compilationUnit: ASTCompilationUnit? = null
    private var firstArrayListNode: ASTClassOrInterfaceType? = null

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTClassOrInterfaceType, data: Any?): Any? {
        val typeName = node.image
        if (typeName != null && typeName.contains("ArrayList")) {
            if (firstArrayListNode == null) {
                firstArrayListNode = node
            }
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx != null && firstArrayListNode != null) {
            addViolationWithMessage(
                ctx,
                firstArrayListNode,
               message,
                0,
                0
            )
        }
        super.end(ctx)
    }
}