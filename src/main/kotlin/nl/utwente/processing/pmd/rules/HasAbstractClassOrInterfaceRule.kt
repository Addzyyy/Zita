package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that flags if the program declares any abstract class or interface (including inner classes).
 */
class HasAbstractClassOrInterfaceRule : AbstractJavaRule() {
    private var found: ASTClassOrInterfaceDeclaration? = null

    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any?): Any? {
        if ((node.isAbstract || node.isInterface) && found == null) {
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
