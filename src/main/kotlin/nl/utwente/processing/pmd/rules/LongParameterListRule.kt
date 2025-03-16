package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.rule.design.ExcessiveNodeCountRule
import net.sourceforge.pmd.lang.rule.stat.StatisticalRule
import net.sourceforge.pmd.stat.DataPoint
import net.sourceforge.pmd.util.NumericConstants

/**
 * Class which implements the long parameter list smell as PMD rule. Based on the ExcessiveParameterListRule in PMD.
 */
class LongParameterListRule : ExcessiveNodeCountRule(ASTFormalParameters::class.java) {

    init {
        setProperty(StatisticalRule.MINIMUM_DESCRIPTOR, 6.0)
    }

    override fun visit(node: ASTFormalParameter, data: Any): Any {
        return NumericConstants.ONE
    }

    override fun getViolationParameters(point: DataPoint?): Array<Any> {
        val parent = point?.node?.jjtGetParent()
        val reference = when(parent) {
            is ASTConstructorDeclaration -> "constructor " + parent.getFirstParentOfType(ASTClassOrInterfaceDeclaration::class.java).image
            is ASTMethodDeclaration -> parent.methodName
            else -> "(null)"
        }
        val score = point?.score?.toInt()
        return arrayOf(reference, score.toString())
    }

}