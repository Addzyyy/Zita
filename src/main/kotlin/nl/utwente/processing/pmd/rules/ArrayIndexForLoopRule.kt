package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTForStatement
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclaratorId
import net.sourceforge.pmd.lang.java.ast.ASTType
import net.sourceforge.pmd.lang.java.ast.ASTBlockStatement
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Flags if arrays are not iterated using a for loop with an index/counter.
 * If an index-based for loop is found iterating an array, does not flag.
 * If only for-each loops or no loops are used, flags the code.
 */
class ArrayIndexForLoopRule : AbstractJavaRule() {
    private var foundIndexBasedLoop = false
    private var compilationUnit: ASTCompilationUnit? = null
    override fun visit(node: ASTCompilationUnit?, data: Any?): Any? {
        if (node != null) {
            compilationUnit = node
        }
        return super.visit(node, data)
    }

    override fun visit(node: ASTForStatement, data: Any?): Any? {
        // Get the loop variable name (e.g., "i")
        val loopVarName = getLoopVariable(node)

        if (loopVarName != null) {
            // In PMD 6.x, array access is represented by ASTPrimarySuffix with image "["
            val suffixes = node.findDescendantsOfType(net.sourceforge.pmd.lang.java.ast.ASTPrimarySuffix::class.java)
            for (suffix in suffixes) {
                if (suffix.isArrayDereference) {
                    // Check if the array index contains the loop variable
                    val suffixText = suffix.toString()
                    if (suffixText.contains(loopVarName)) {
                        foundIndexBasedLoop = true
                        break
                    }
                }
            }
        }

        return super.visit(node, data)
    }

    private fun getLoopVariable(forStmt: ASTForStatement): String? {
        val varDecls = forStmt.findDescendantsOfType(ASTVariableDeclaratorId::class.java)
        return varDecls.firstOrNull()?.image
    }

    override fun end(ctx: net.sourceforge.pmd.RuleContext?) {
        if (!foundIndexBasedLoop) {
            addViolationWithMessage(
                ctx,
                compilationUnit,
               message,
                0,
                0
            )
        }
        super.end(ctx)
    }
}
