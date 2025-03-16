package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.metrics.api.JavaOperationMetricKey
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.lang.metrics.MetricsUtil
import nl.utwente.processing.pmd.symbols.ProcessingApplet
import nl.utwente.processing.pmd.symbols.ProcessingAppletMethodCategory
import java.lang.Exception
import java.lang.IllegalStateException

/**
 * Class which implements the long method smell as PMD rule. Based on the NcssMethodCountRule in PMD. The class has
 * been modified to count blocks of Processing shape drawing methods as one line.
 */
class LongMethodRule : AbstractJavaRule() {

    // List to store all methods that can be within a block, can be expanded as needed in the future.
    private val targetMethods = listOf<ProcessingAppletMethodCategory>(
            ProcessingAppletMethodCategory.SHAPE,
            ProcessingAppletMethodCategory.SHAPE_2D,
            ProcessingAppletMethodCategory.SHAPE_CURVES,
            ProcessingAppletMethodCategory.SHAPE_3D,
            ProcessingAppletMethodCategory.SHAPE_ATTRIBUTES,
            ProcessingAppletMethodCategory.SHAPE_VERTEX,
            ProcessingAppletMethodCategory.SHAPE_LD
    )

    /**
     *  Visitor goes through all method declarations and first uses the standard PMD NCSS method to get the
     *  normal count. After which the expressions list is stored within and a block offset is defined.
     *  The offset is calculated by identifying a shape method and from there incrementing the offset until the
     *  first non shape method call or statement is reached.
     */
    override fun visit(node: ASTMethodDeclaration, data: Any?): Any? {
        val ncss = MetricsUtil.computeMetric(JavaOperationMetricKey.NCSS, node)

        if (!node.hasDescendantOfType(ASTStatementExpression::class.java))  return super.visit(node, data) // Filters abstract classes.

        val expressions = node.body.findDescendantsOfType(ASTStatementExpression::class.java)
        var blockOffset = 0.0
        var i = 0
        while (i < expressions.size) {
            try {
                if (!expressions[i].hasDescendantOfType(ASTAssignmentOperator::class.java)) {
//                    println(expressions[i].getFirstDescendantOfType(ASTPrimaryPrefix::class.java).getFirstDescendantOfType(ASTName::class.java).image);
                    val method = expressions[i].getFirstDescendantOfType(ASTPrimaryPrefix::class.java)
                    var methodName = ""
                    if (method.hasDescendantOfType(ASTName::class.java)) {
                        methodName = method.getFirstDescendantOfType(ASTName::class.java).image
                    }
                    if (isShapeMethod(methodName) && i < expressions.size - 1) {
                        for (j in i + 1 until expressions.size) {
                            val nextMethodName = expressions[j].getFirstDescendantOfType(ASTPrimaryPrefix::class.java)
                                .getFirstDescendantOfType(ASTName::class.java).image
                            if (!isShapeMethod(nextMethodName)) {
                                i = j
                                break
                            }
                            blockOffset++
                        }
                    }
                }
            }
            catch (e: IllegalStateException) {
                // Suppress Exception
            }
            // We don't care about the exception, because it just indicates that this is not the pattern we are looking for
            // and we should just count this statement as a single line
            finally {
                i++
            }
        }
        if ((ncss - blockOffset) > 50.0) {
            this.addViolationWithMessage(data, node.getFirstDescendantOfType(ASTResultType::class.java), message, arrayOf(node.name, ncss))
        }
        return super.visit(node, data)
    }

    /**
     * Small function that check whether the method that is given is a shaped method.
     *
     * @param methodName
     * @return Boolean if the method is defined as a shape method within target methods.
     */
    private fun isShapeMethod(methodName: String): Boolean {
        ProcessingApplet.DRAW_METHODS.forEach {
            if (it.name == methodName && targetMethods.contains(it.category)) {
                return true
            }
        }
        return false
    }
}