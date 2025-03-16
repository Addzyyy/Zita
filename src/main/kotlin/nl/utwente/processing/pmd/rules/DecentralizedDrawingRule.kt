package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import nl.utwente.processing.pmd.symbols.ProcessingApplet
import nl.utwente.processing.pmd.utils.matches

/**
 * Class which implements the decentralized event handling smell as PMD rule.
 */
class DecentralizedDrawingRule: AbstractJavaRule() {

    private var drawMethod: ASTMethodDeclaration? = null

    private val restrictedMethods =  ProcessingApplet.EVENT_METHOD_SIGNATURES + ProcessingApplet.SETUP_METHOD_SIGNATURE


    override fun visit(node: ASTCompilationUnit?, data: Any?): Any? {
        this.drawMethod = null
        return super.visit(node, data)
    }

    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any): Any? {
        //Check if this is a top node, not a inner class.
        if (!node.isNested) {


            val drawMethodDecl = node.findDescendantsOfType(ASTMethodDeclaration::class.java).firstOrNull() { it.name == "draw" }

            this.drawMethod = drawMethodDecl

        }
        return super.visit(node, data)
    }

    override fun visit(node: ASTPrimaryExpression, data: Any): Any? {

        val method = node.getFirstParentOfType(ASTMethodDeclaration::class.java)

        val match = node.matches(*ProcessingApplet.DRAW_METHODS.map { it.name }.toTypedArray())


        if (match != null && method != drawMethod) {

            val methodName = method?.name ?: "unknown"



            if (restrictedMethods.contains("$methodName()")) {

                this.addViolationWithMessage(data, node, message, kotlin.arrayOf(match, method.name))
            }
        }
        return super.visit(node, data)
    }
}