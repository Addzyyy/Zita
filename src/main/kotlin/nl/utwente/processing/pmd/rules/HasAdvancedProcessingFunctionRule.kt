package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryPrefix
import net.sourceforge.pmd.lang.java.ast.ASTName
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Flags use of advanced Processing functions like translate, rotate, and matrix operations.
 * Useful for tutors to quickly find places to quiz students on more complex concepts.
 */
class HasAdvancedProcessingFunctionRule : AbstractJavaRule() {

    private val advancedProcessingFunctions = setOf(
        "rotate", "scale", "shearX", "shearY", "applyMatrix",
        "pushMatrix", "popMatrix",
        "beginShape", "endShape", "curveVertex", "bezierVertex",
        "texture", "textureMode", "get", "set", "loadPixels", "updatePixels",
        "loadImage", "image", "loadFont", "textFont", "createFont",
        "frameRate", "frameCount", "millis",
        "loadStrings", "saveStrings", "loadTable", "saveTable",
        "loadJSONObject", "saveJSONObject", "loadXML", "saveXML"
    )

    override fun visit(node: ASTPrimaryExpression, data: Any): Any {
        val prefix = node.getFirstChildOfType(ASTPrimaryPrefix::class.java)
        val nameNode = prefix?.getFirstChildOfType(ASTName::class.java)

        val methodName = nameNode?.image?.substringAfterLast(".") ?: return super.visit(node, data)

        if (methodName in advancedProcessingFunctions) {
            addViolationWithMessage(
                data as RuleContext,
                node,
                "Advanced Processing function '$methodName' used. Tutors should review this with the student to assess understanding.",
                0,0
            )
        }

        return super.visit(node, data)
    }
}