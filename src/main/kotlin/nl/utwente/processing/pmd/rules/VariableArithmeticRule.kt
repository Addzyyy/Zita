package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.properties.PropertyDescriptor
import net.sourceforge.pmd.properties.PropertyFactory

class VariableArithmeticRule : AbstractJavaRule() {

    companion object {
        private val CATEGORY: PropertyDescriptor<String> =
            PropertyFactory.stringProperty("category")
                .desc("Rule category")
                .defaultValue("default")
                .build()
    }
    init {
        definePropertyDescriptor(CATEGORY)
    }
    private var meaningfulArithmeticCount = 0
    private var referenceNode: Node? = null
    private var compilationUnit: ASTCompilationUnit? = null

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTAdditiveExpression, data: Any?): Any? {
        if (hasVariableOperand(node)) {
            meaningfulArithmeticCount++
            if (referenceNode == null) referenceNode = node
        }
        return super.visit(node, data)
    }

    override fun visit(node: ASTMultiplicativeExpression, data: Any?): Any? {
        if (hasVariableOperand(node)) {
            meaningfulArithmeticCount++
            if (referenceNode == null) referenceNode = node
        }
        return super.visit(node, data)
    }

    override fun visit(node: ASTStatementExpression, data: Any?): Any? {
        val assignmentOp = node.getFirstDescendantOfType(ASTAssignmentOperator::class.java)
        if (assignmentOp != null) {
            val operator = assignmentOp.image
            if (operator in listOf("+=", "-=", "*=", "/=", "%=")) {
                meaningfulArithmeticCount++
                if (referenceNode == null) referenceNode = node
            }
        }
        return super.visit(node, data)
    }

    override fun visit(node: ASTUnaryExpression, data: Any?): Any? {
        val operator = node.image
        if (operator == "++" || operator == "--") {
            meaningfulArithmeticCount++
            if (referenceNode == null) referenceNode = node
        }
        return super.visit(node, data)
    }

    override fun visit(node: ASTPreIncrementExpression, data: Any?): Any? {
        meaningfulArithmeticCount++
        if (referenceNode == null) referenceNode = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTPreDecrementExpression, data: Any?): Any? {
        meaningfulArithmeticCount++
        if (referenceNode == null) referenceNode = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTPostfixExpression, data: Any?): Any? {
        if (node.image == "++" || node.image == "--") {
            meaningfulArithmeticCount++
            if (referenceNode == null) referenceNode = node
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx != null && meaningfulArithmeticCount < 2) {
            val nodeToReport = referenceNode ?: compilationUnit

            if (nodeToReport != null) {
                addViolationWithMessage(
                    ctx,
                    nodeToReport,
                    message,
                    0,
                    0
                )
            }
        }
        super.end(ctx)
    }

    private fun hasVariableOperand(node: JavaNode): Boolean {
        return node.findDescendantsOfType(ASTName::class.java).isNotEmpty()
    }
}