package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTFieldDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.lang.ast.Node

/// Rule that flags the use of any access modifier (private, public, protected, or package-private) on variables (fields) and functions (methods)
class HasAccessModifierRule : AbstractJavaRule() {
    private var found = false
    private var referenceNode: Node? = null

    private fun hasAccessModifier(node: ASTFieldDeclaration): Boolean {
        return node.isPrivate || node.isPublic || node.isProtected
    }

    private fun hasAccessModifier(node: ASTMethodDeclaration): Boolean {
        return node.isPrivate || node.isPublic || node.isProtected
    }

    override fun visit(node: ASTFieldDeclaration?, data: Any?): Any? {
        if (node != null && hasAccessModifier(node)) {
            found = true
            if (referenceNode == null) referenceNode = node
        }
        return super.visit(node, data)
    }

    override fun visit(node: ASTMethodDeclaration?, data: Any?): Any? {
        if (node != null && hasAccessModifier(node)) {
            found = true
            if (referenceNode == null) referenceNode = node
        }
        return super.visit(node, data)
    }

    override fun end(ctx: RuleContext?) {
        val node = referenceNode
        if (node != null && ctx != null) {
            addViolationWithMessage(
                ctx,
                node,
                message,
                node.beginLine,
                node.endLine
            )
        }
    }
    }

