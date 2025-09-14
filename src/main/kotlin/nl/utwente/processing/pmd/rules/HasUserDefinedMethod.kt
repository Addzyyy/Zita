

package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

class HasUserDefinedMethod : AbstractJavaRule() {
    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        val methodNodes = node.findDescendantsOfType(ASTMethodDeclaration::class.java)
        // Ignore methods named 'draw' and 'setup'
        val userDefinedMethods = methodNodes.filter {
            val name = it.name
            name != "draw" && name != "setup"
        }
        if (userDefinedMethods.isEmpty()) {
            addViolationWithMessage(data, node, message, 0, 0)
        }
        return super.visit(node, data)
    }
}
