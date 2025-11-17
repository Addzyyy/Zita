package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTClassOrInterfaceDeclaration
import net.sourceforge.pmd.lang.java.ast.ASTConstructorDeclaration
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule

/**
 * Rule that checks whether there is at least one user-defined constructor in each class in the code.
 * A user-defined constructor is defined as a constructor that matches the class name.
 * If no such constructor is found in a class, a violation is reported.
 */
class HasUserDefinedConstructor: AbstractJavaRule() {

    private var outerClass: ASTClassOrInterfaceDeclaration? = null

    override fun visit(node: ASTClassOrInterfaceDeclaration?, data: Any?): Any? {
        if (node == null) return data

        if (outerClass == null) {
            // First class is assumed to be the top-level wrapper
            outerClass = node
            return super.visit(node, data)
        }

        // Skip interfaces or top-level class itself
        if (node == outerClass || node.isInterface) {
            return super.visit(node, data)
        }

        val hasConstructor = node.findDescendantsOfType(ASTConstructorDeclaration::class.java)
            .any {it.image == null || it.image == node.image}

        // Check for presence of constructor(s)


        if (!hasConstructor) {
            addViolationWithMessage(
                data,
                node,
              message,
                0,
                0
            )
        }


        return super.visit(node, data)
    }
}