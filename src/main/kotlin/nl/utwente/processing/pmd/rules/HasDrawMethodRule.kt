package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTPrimaryExpression
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.lang.java.symboltable.ClassScope
import nl.utwente.processing.pmd.symbols.ProcessingApplet
import nl.utwente.processing.pmd.utils.findMethods
import nl.utwente.processing.pmd.utils.matches
import nl.utwente.processing.pmd.utils.uniqueCallStack

/**
 * Class which implements the decentralized event handling smell as PMD rule.
 */
class HasDrawMethodRule: AbstractJavaRule() {

    private var eventStack: Set<ASTMethodDeclaration> = emptySet();

    override fun visit(node: ASTCompilationUnit?, data: Any?): Any? {
        this.eventStack = emptySet()
        return super.visit(node, data)
    }

    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any): Any? {
        //Check if this is a top node, not a inner class.
        if (!node.isNested) {
            val scope = node.scope as? ClassScope
            val methodDecls = scope?.findMethods(ProcessingApplet.DRAW_METHOD_SIGNATURE);
            if (methodDecls.isNullOrEmpty()) {
                addViolationWithMessage(data, node, message)
            } else {
                this.eventStack = scope.uniqueCallStack(*methodDecls)
            }
        }
        return super.visit(node, data)
    }
}