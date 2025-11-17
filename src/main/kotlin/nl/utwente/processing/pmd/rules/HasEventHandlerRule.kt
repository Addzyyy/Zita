package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.lang.java.symboltable.ClassScope
import net.sourceforge.pmd.properties.PropertyDescriptor
import net.sourceforge.pmd.properties.PropertyFactory
import nl.utwente.processing.pmd.symbols.ProcessingApplet
import nl.utwente.processing.pmd.utils.findMethods


/// ** Class which implements the hasEventHandler smell as PMD rule. */
class HasEventHandlerRule: AbstractJavaRule() {

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

    override fun visit(node: ASTCompilationUnit?, data: Any?): Any? {
        return super.visit(node, data)
    }

    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any): Any? {
        //Check if this is a top node, not a inner class.
        if (!node.isNested) {
            val scope = node.scope as? ClassScope
            val methodDecls = scope?.findMethods(ProcessingApplet.EVENT_METHOD_SIGNATURES)
            if (methodDecls.isNullOrEmpty()) {
                // setting line and column to 0, as this is a class level violation and does not have a specific line
                addViolationWithMessage(data, node, message,0,0)
            }
        }
        return super.visit(node, data)
    }
}