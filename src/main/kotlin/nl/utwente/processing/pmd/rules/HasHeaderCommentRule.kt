package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.properties.PropertyDescriptor
import net.sourceforge.pmd.properties.PropertyFactory

/**
 * Rule that checks whether the code contains a header comment with a link to "nga.gov.au".
 * If such a comment is not found, a violation is reported.
 */
class HasHeaderCommentRule : AbstractJavaRule() {


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
    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        // Get comments from the compilation unit
        val comments = node.comments

        var hasLinkInComment = false

        if (comments != null && comments.isNotEmpty()) {
            for (comment in comments) {
                
                val commentText = comment.image ?: ""

                if (commentText.contains("nga.gov.au", ignoreCase = true)) {
                    hasLinkInComment = true
                    break
                }
            }
        }

        if (!hasLinkInComment) {
            addViolationWithMessage(
                data, node,
                message
            )
        }

        return super.visit(node, data)
    }
}