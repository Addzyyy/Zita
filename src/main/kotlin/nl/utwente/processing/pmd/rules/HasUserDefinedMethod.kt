

package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmd.symbols.ProcessingApplet

class HasUserDefinedMethod : AbstractJavaRule() {
    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        val methodNodes = node.findDescendantsOfType(ASTMethodDeclaration::class.java)
        // Get event method names (without parameters)
        val eventMethodNames = ProcessingApplet.EVENT_METHOD_SIGNATURES
            .map { it.substringBefore("(") }
            .toSet()
        // Ignore methods named 'draw', 'setup', and event methods
        val userDefinedMethods = methodNodes.filter {
            val name = it.name
            name != "draw" && name != "setup" && name !in eventMethodNames
        }
        if (userDefinedMethods.isEmpty()) {
            addViolationWithMessage(data, node, message, 0, 0)
        }
        return super.visit(node, data)
    }
}
