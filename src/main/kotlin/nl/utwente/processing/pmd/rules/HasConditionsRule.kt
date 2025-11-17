package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTIfStatement
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.properties.PropertyDescriptor
import net.sourceforge.pmd.properties.PropertyFactory

/**
 * Rule that checks whether there are at least two conditional statements (if statements) in the code.
 * If fewer than two conditions are found, a violation is reported.
 */
class HasConditionsRule : AbstractJavaRule() {



    private var conditions = 0
    private var firstNode: Node? = null


    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        if (firstNode == null) firstNode = node
        return super.visit(node, data)
    }


    override fun visit(node: ASTIfStatement, data: Any?): Any? {
        conditions += 1
        return  super.visit(node, data)
    }

    override fun end(ctx: RuleContext) {
        if (conditions < 2 && firstNode != null) {
            addViolationWithMessage(ctx, firstNode!!, message, 0, 0)
        }
        super.end(ctx)
    }
}