package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTType
import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTLocalVariableDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTVariableDeclarator
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.RuleContext

class HasArrayUsageRule : AbstractJavaRule() {

    private var hasArray = false
    private var compilationUnit: ASTCompilationUnit? = null

    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        hasArray = false
        compilationUnit = node
        return super.visit(node, data)
    }
    override fun visit(node: ASTFieldDeclaration, data: Any?): Any? {
        // Get the type for this field
        val typeNode = node.getFirstChildOfType(ASTType::class.java)
        val baseTypeName = typeNode?.typeImage ?: "unknown"

        node.findChildrenOfType(ASTVariableDeclarator::class.java).forEach { declarator ->

            val varId = declarator.variableId
            val hasVariableArrayDimensions = varId.isArray
            val isArray = (typeNode?.isArrayType ?: false) || hasVariableArrayDimensions
            val typeName = if (isArray) {
                "$baseTypeName[]"
            } else {
                baseTypeName
            }

            // Mark that we found an array
            if (isArray) {
                hasArray = true
            }
        }

        return super.visit(node, data)
    }

    override fun visit(node: ASTLocalVariableDeclaration, data: Any?): Any? {
        // Get the type for this local variable
        val typeNode = node.getFirstChildOfType(ASTType::class.java)
        val baseTypeName = typeNode?.typeImage ?: "unknown"

        node.findChildrenOfType(ASTVariableDeclarator::class.java).forEach { declarator ->

            val varId = declarator.variableId
            val hasVariableArrayDimensions = varId.isArray
            val isArray = (typeNode?.isArrayType ?: false) || hasVariableArrayDimensions
            val typeName = if (isArray) {
                "$baseTypeName[]"
            } else {
                baseTypeName
            }

            // Mark that we found an array
            if (isArray) {
                hasArray = true
            }
        }

        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        if (!hasArray && ctx != null && compilationUnit != null) {
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