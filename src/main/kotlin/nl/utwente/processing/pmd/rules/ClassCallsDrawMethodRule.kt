package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmd.symbols.ProcessingApplet

/**
 * Rule that requires at least one inner class to have a method that calls a drawing function.
 * PASS: At least one inner class calls a drawing method
 * FAIL: No inner classes found OR no inner classes call drawing methods
 */
class ClassCallsDrawMethodRule : AbstractJavaRule() {
    private var compilationUnit: ASTCompilationUnit? = null
    private var hasInnerClass = false
    private var hasInnerClassThatDraws = false

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any?): Any? {
        if (node.isNested) {
            hasInnerClass = true

            // Check if this inner class has methods that call drawing functions
            val methods = node.findDescendantsOfType(ASTMethodDeclaration::class.java)
            for (methodDecl in methods) {
                val methodCalls = methodDecl.findDescendantsOfType(ASTPrimaryExpression::class.java)
                for (call in methodCalls) {
                    val image = getMethodCallName(call)
                    if (image != null && ProcessingApplet.DRAW_METHODS.any { it.name == image }) {
                        hasInnerClassThatDraws = true
                        return super.visit(node, data)  // Found one, we're done
                    }
                }
            }
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx != null && compilationUnit != null) {
            if (!hasInnerClass) {

                addViolationWithMessage(
                    ctx,
                    compilationUnit,
                    "No classes were found. Try adding a class to organize your drawing code — for example, create a class with a `draw` or `display` method to keep things clear and testable. if you need any help udnersatanding this rule your TA is there to help",
                    0,
                    0
                )

            } else if (!hasInnerClassThatDraws) {
                // Inner classes exist but none call drawing methods
                addViolationWithMessage(
                    ctx,
                    compilationUnit,
                    "Classes exist but none call drawing methods. Try adding a class with a `draw` or `display` method to organize your drawing code and make it easier to test and understand. if you need any help udnersatanding this rule your TA is there to help",
                    0,
                    0
                )

            }
            // If hasInnerClassThatDraws is true, PASS (no violation)
        }
        super.end(ctx)
    }

    private fun getMethodCallName(node: ASTPrimaryExpression): String? {
        val prefix = node.getFirstChildOfType(net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix::class.java)
        val nameNode = prefix?.getFirstChildOfType(net.sourceforge.pmd.lang.java.ast.ASTName::class.java)
        return nameNode?.image?.substringBefore('.')
    }
}