package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that checks whether there is at least one user-defined class in the code.
 * A user-defined class is defined as a class that contains at least one inner class.
 * If no such class is found, a violation is reported.
 */
class HasUserDefinedClass: AbstractJavaRule() {

    private var checkedOuter = false

    override fun visit(node: ASTClassOrInterfaceDeclaration?, data: Any?): Any? {
        if (node == null || checkedOuter || node.isNested) return super.visit(node, data)

        // This is the outermost class
        checkedOuter = true

        val hasInnerClass = node.findDescendantsOfType(ASTClassOrInterfaceDeclaration::class.java)
            .any { it != node } // Exclude the outer class itself


        if (!hasInnerClass) {
            addViolationWithMessage(data, node,message,0,0)
        }

        return super.visit(node, data)
    }
}