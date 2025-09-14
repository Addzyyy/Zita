package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTType
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

class HasArrayUsageRule : AbstractJavaRule() {

    private var hasArray = false

    override fun visit(node: ASTType, data: Any?): Any? {
        if (node.isArrayType) {
            hasArray = true
        }
        return super.visit(node, data)
    }

    override fun visit(node: ASTCompilationUnit?, data: Any?): Any? {
        hasArray = false
        return super.visit(node, data).also {
            if (!hasArray && node != null) {
                addViolationWithMessage(
                    data,
                    node,
                    message,
                    0,
                    0
                )
            }
        }
    }
}