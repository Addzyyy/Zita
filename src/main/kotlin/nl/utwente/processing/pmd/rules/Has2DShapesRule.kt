package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.RuleContext
import nl.utwente.processing.pmd.symbols.ProcessingApplet
import nl.utwente.processing.pmd.symbols.ProcessingAppletMethodCategory
import nl.utwente.processing.pmd.utils.matches
import net.sourceforge.pmd.lang.ast.Node
import net.sourceforge.pmd.properties.PropertyDescriptor
import net.sourceforge.pmd.properties.PropertyFactory

class Has2DShapesRule : AbstractJavaRule() {

    companion object {
        private val CATEGORY: PropertyDescriptor<String> =
            PropertyFactory.stringProperty("category")
                .desc("Rule category")
                .defaultValue("default")
                .build()
    }

    init {
        definePropertyDescriptor(CATEGORY)
    }

    private var shape2DCount = 0
    private var firstShapeNode: Node? = null
    private var compilationUnit: ASTCompilationUnit? = null

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        // Store the compilation unit so we always have a node to report against
        compilationUnit = node
        return super.visit(node, data)
    }

    override fun visit(node: ASTPrimaryExpression, data: Any): Any? {
        if (
            ProcessingApplet.DRAW_METHODS
                .filter { it.category == ProcessingAppletMethodCategory.SHAPE_2D }
                .any { shapeMethod -> node.matches(shapeMethod) }
        ) {
            shape2DCount++
            if (firstShapeNode == null) {
                firstShapeNode = node
            }
        }

        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (ctx != null && shape2DCount < 2) {
            val nodeToReport = firstShapeNode ?: compilationUnit

            if (nodeToReport != null) {
                addViolationWithMessage(
                    ctx,
                    nodeToReport,
                    message,
                    0,
                    0
                )
            }
        }
        super.end(ctx)
    }
}