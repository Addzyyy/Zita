package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

class VariableAndArithmeticRule: AbstractJavaRule() {

    private var variableCount = 0
    private var arithmeticCount = 0

    override fun visit(node: ASTCompilationUnit?, data: Any?): Any? {
        variableCount = 0
        arithmeticCount = 0
        return super.visit(node, data).also {
            if (variableCount < 2 || arithmeticCount < 2)
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