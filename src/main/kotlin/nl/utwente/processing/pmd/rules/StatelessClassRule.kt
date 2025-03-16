package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.lang.java.symboltable.ClassScope
import nl.utwente.processing.pmd.AbstractProcessingRule

/**
 * Class which implements the stateless class smell as PMD rule.
 */
class StatelessClassRule : AbstractProcessingRule() {

    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any): Any? {
        //Check if this is a top node, not a inner class.
        if (node.isNested && !node.isInterface && !node.isAbstract) {
            val scope = node.scope as? ClassScope
            val vars = scope?.variableDeclarations?.size ?: 0
            if (vars == 0) {
                this.addViolationWithMessage(data, node, message, node.beginLine, node.beginLine, kotlin.arrayOf(scope?.className))
            }
        }
        return super.visit(node, data)
    }
}