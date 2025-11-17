package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that flags if the program declares a class inside a class inside a class (triple nested class).
 */
class HasTripleNestedClassRule : AbstractJavaRule() {
    private var found: ASTClassOrInterfaceDeclaration? = null

    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any?): Any? {
        var nestingLevel = 0
        var parent = node.jjtGetParent()
        while (parent != null) {
            if (parent is ASTClassOrInterfaceDeclaration) {
                nestingLevel++
            }
            parent = parent.jjtGetParent()
        }
        if (nestingLevel >= 2 && found == null) { // 2 parents = triple nested
            found = node
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (found != null) {
            addViolationWithMessage(
                ctx,
                found,
                message,
                0,
                0
            )
        }
        super.end(ctx)
    }
}
