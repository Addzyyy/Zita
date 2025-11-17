package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that checks whether there is at least one function/method with parameters defined in the code.
 * If no such function is found, a violation is reported.
 */
class HasFunctionWithParametersRule : AbstractJavaRule() {

    private var found = false
    private var compilationUnit: ASTCompilationUnit? = null

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTMethodDeclaration, data: Any?): Any? {
        if (node.formalParameters.size() > 0) {
            found = true
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (!found && compilationUnit != null && ctx != null) {
            addViolationWithMessage(
                ctx,
                compilationUnit!!,
                message,
                0,0
            )
        }
        super.end(ctx)
    }
}