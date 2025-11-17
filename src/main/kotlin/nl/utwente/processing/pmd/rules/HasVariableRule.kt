 package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that checks whether there is at least one variable declared in the code.
 * If no variable is found, a violation is reported.
 */
class HasVariableRule : AbstractJavaRule() {
    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        val hasVariable = node.findDescendantsOfType(ASTVariableDeclarator::class.java).isNotEmpty()
        if (!hasVariable) {
            addViolationWithMessage(data, node, message, 0, 0)
        }
        return super.visit(node, data)
    }
}
