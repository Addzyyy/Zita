package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that flags if students are using inheritance through the extends keyword.
 * Passes if:
 * - No user-defined classes use inheritance
 *
 * Fails if:
 * - At least one user-defined class extends another class
 */
class HasInheritanceRule : AbstractJavaRule() {

    private var compilationUnit: ASTCompilationUnit? = null
    private var hasInheritance = false
    private var outerClassName: String? = null

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any?): Any? {
        if (node.isInterface) return super.visit(node, data)

        // Skip the outer Processing wrapper class
        if (outerClassName == null) {
            outerClassName = node.image
            return super.visit(node, data)
        }

        // Check if this class extends another class
        val extendsList = node.getFirstChildOfType(
            net.sourceforge.pmd.lang.java.ast.ASTExtendsList::class.java
        )

        if (extendsList != null && extendsList.jjtGetNumChildren() > 0) {
            hasInheritance = true
        }

        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx != null && compilationUnit != null && hasInheritance) {
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
