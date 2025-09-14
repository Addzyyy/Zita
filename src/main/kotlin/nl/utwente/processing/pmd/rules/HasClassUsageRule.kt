package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceType
import net.sourceforge.pmd.lang.java.ast.JavaNode
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Flags usage of advanced Java/Processing classes not expected in beginner assignments.
 * Outputs one violation per detected type, including line numbers and class name.
 */
class HasClassUsageRule : AbstractJavaRule() {

    private val forbiddenTypes = listOf(
        // Data structures and utility classes (suspicious for COMP1000)
        "ArrayList", "HashMap", "HashSet", "TreeMap", "TreeSet", "LinkedList",
        "Deque", "Stack", "Queue",

        // Math-related (beyond expected beginner scope)
        "Math", "BigDecimal", "BigInteger", "DecimalFormat", "NumberFormat",

        // Graphics/Processing-related (advanced or copy-paste indicators)
        "PVector", "Graphics2D", "AffineTransform", "Color",

        // JSON and parsing (very uncommon for simple sketches)
        "Gson", "JSONObject", "JsonObject"
    )

    private var arrayListNode: JavaNode? = null
    private var firstForbiddenNode: JavaNode? = null
    private var firstForbiddenType: String? = null

    override fun visit(node: ASTClassOrInterfaceType, data: Any?): Any? {
        val className = node.image ?: return super.visit(node, data)

        if (className in forbiddenTypes) {
            val javaNode = node as JavaNode

            if (className == "ArrayList" && arrayListNode == null) {
                arrayListNode = javaNode
            } else if (firstForbiddenNode == null) {
                firstForbiddenNode = javaNode
                firstForbiddenType = className
            }
        }

        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx == null) return

        if (arrayListNode != null) {
            val msg = "Submission uses 'ArrayList' Consider asking the student about its purpose."
            addViolationWithMessage(ctx, arrayListNode, msg, arrayListNode!!.beginLine, arrayListNode!!.endLine)
        } else if (firstForbiddenNode != null && firstForbiddenType != null) {
            val msg = "Submission uses '$firstForbiddenType'.Consider asking the student about its purpose."
            addViolationWithMessage(ctx, firstForbiddenNode, msg, firstForbiddenNode!!.beginLine, firstForbiddenNode!!.endLine)
        }

        super.end(ctx)
    }
}
