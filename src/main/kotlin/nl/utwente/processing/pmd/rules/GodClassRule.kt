package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.*
import net.sourceforge.pmd.lang.java.symboltable.ClassScope
import net.sourceforge.pmd.lang.java.symboltable.SourceFileScope
import net.sourceforge.pmd.lang.java.symboltable.VariableNameDeclaration
import net.sourceforge.pmd.lang.symboltable.Scope
import net.sourceforge.pmd.util.StringUtil
import nl.utwente.processing.pmd.AbstractProcessingRule
import nl.utwente.processing.pmd.utils.increment

/**
 * Class which implements the God Class design smell as PMD rule. Heavily based on the original PMD implementation,
 * except this implementation treats inner classes as different classes.
 */
class GodClassRule : AbstractProcessingRule() {

    /**
     * If debug is true, always print a violation, even when metrics values are still within limits.
     */
    private val DEBUG = false

    /**
     * Very high threshold for WMC (Weighted Method Count). See: Lanza. Object-Oriented Metrics in Practice. Page 16.
     */
    private val WMC_VERY_HIGH = 47

    /**
     * Few means between 2 and 5. See: Lanza. Object-Oriented Metrics in Practice. Page 18.
     */
    private val FEW_THRESHOLD = 5

    /**
     * One third is a low value. See: Lanza. Object-Oriented Metrics in Practice. Page 17.
     */
    private val ONE_THIRD_THRESHOLD = 1.0 / 3.0

    /**
     * The Weighted Method Count metric, per class.
     */
    private val wmcCounter: MutableMap<ClassScope, Int> = HashMap()

    /**
     * The Access To Foreign Data metric, per class.
     */
    private val atfdCounter: MutableMap<ClassScope, Int> = HashMap()

    /**
     * Collects for each method of the each class, which local attributes are accessed.
     */
    private val methodAttributeAccess: MutableMap<ClassScope, MutableMap<String, MutableSet<String>>> = HashMap()

    /**
     * The current class scope to visit nodes of.
     */
    private var currentClassScope: ClassScope? = null;

    /**
     * The name of the current method.
     */
    private var currentMethodName: String? = null

    /**
     * For each compilation unit, clear all attributes (if the class is reused).
     */
    override fun visit(node: ASTCompilationUnit, data: Any): Any? {
        this.wmcCounter.clear()
        this.atfdCounter.clear()
        this.methodAttributeAccess.clear()
        this.currentClassScope = null
        return super.visit(node, data)
    }

    /**
     * Base entry point for the visitor - a class declaration (everything that belongs to a class). Here the metrics
     * for the specified class are initialized. Then the other nodes are visited. Afterwards te metrics are evaluated
     * against fixed thresholds.
     *
     * The behaviour differs here a little bit from the original PMD implementation, which has the main entry point
     * not on a class, but on a compilation unit, which makes more sense for Java than for Processing.
     */
    override fun visit(node: ASTClassOrInterfaceDeclaration, data: Any): Any? {
        val oldScope = this.currentClassScope
        val scope = node.scope as ClassScope
        this.wmcCounter.put(scope, 0)
        this.atfdCounter.put(scope, 0)
        this.methodAttributeAccess.put(scope, HashMap())
        this.currentClassScope = scope

        val result = super.visit(node, data)
        val tcc = this.calculateTcc(scope)

        if ((this.wmcCounter.getOrDefault(scope, 0) >= WMC_VERY_HIGH &&
                        this.atfdCounter.getOrDefault(scope, 0) > FEW_THRESHOLD &&
                        tcc < ONE_THIRD_THRESHOLD) || DEBUG) {
                            // TODO: Main tab not checked, ask Ansgar.
            this.addViolationWithMessage(data, node, message, node.beginLine, node.beginLine,
                    kotlin.arrayOf(node.image, this.wmcCounter[scope], this.atfdCounter[scope], tcc))
        }
        this.currentClassScope = oldScope
        return result
    }

    /**
     * Calculates the Tight Class Cohesion metric.
     * @return a value between 0 and 1.
     */
    private fun calculateTcc(scope: ClassScope): Double {
        val methodPairs = determineMethodPairs(scope)
        val totalMethodPairs = calculateTotalMethodPairs(scope)
        return methodPairs / totalMethodPairs
    }

    /**
     * Calculates the number of possible method pairs. Its basically the sum of the first (methodCount - 1) integers.
     * It will be 0, if no methods exist or only one method, means, if no pairs exist.
     * @return The calculated total method pairs.
     */
    private fun calculateTotalMethodPairs(scope: ClassScope): Double {
        val n = (this.methodAttributeAccess[scope]?.size ?: 0) - 1
        return n * (n + 1) / 2.0
    }

    /**
     * Uses the [.methodAttributeAccess] map to detect method pairs, that use at least one common attribute of the class.
     * @return The method pairs that use at least one common attribute in the same class scope.
     */
    private fun determineMethodPairs(scope: ClassScope): Int {
        val methods = ArrayList(this.methodAttributeAccess[scope]?.keys ?: emptyList())
        var pairs = 0

        if (methods.size > 1) {
            for (i in 0..methods.size - 1) {
                for (j in i + 1..methods.size - 1) {
                    val firstMethodName = methods[i]
                    val secondMethodName = methods[j]
                    val accessesOfFirstMethod = this.methodAttributeAccess[scope]?.get(firstMethodName) ?: HashSet()
                    val accessesOfSecondMethod = this.methodAttributeAccess[scope]?.get(secondMethodName) ?: HashSet()
                    val combinedAccesses = HashSet<String>()

                    combinedAccesses.addAll(accessesOfFirstMethod)
                    combinedAccesses.addAll(accessesOfSecondMethod)

                    if (combinedAccesses.size < accessesOfFirstMethod.size + accessesOfSecondMethod.size) {
                        pairs++
                    }
                }
            }
        }

        return pairs
    }

    /**
     * The primary expression node is used to detect access to attributes and method calls. If the access is not for a
     * foreign class, then the [.methodAttributeAccess] map is updated for the current method.
     */
    override fun visit(node: ASTPrimaryExpression, data: Any): Any? {
        if (this.isForeignAttributeOrMethod(node)) {
            if (this.isAttributeAccess(node) || this.isMethodCall(node) && this.isForeignGetterSetterCall(node)) {
                this.atfdCounter.increment(this.currentClassScope)
            }
        } else {
            val currentMethod = this.currentMethodName
            if (currentMethod != null) {
                val methodAccess = this.methodAttributeAccess[this.currentClassScope]?.get(currentMethod)
                val variableName = this.getVariableName(node)
                variableName?.let {
                    val variableDeclaration = this.findVariableDeclaration(it, this.currentClassScope!!)
                    if (variableDeclaration != null) {
                        methodAccess?.add(it)
                    }
                }
            }
        }

        return super.visit(node, data)
    }

    private fun isForeignGetterSetterCall(node: ASTPrimaryExpression): Boolean {
        val methodOrAttributeName = this.getMethodOrAttributeName(node)
        return methodOrAttributeName != null && StringUtil.startsWithAny(methodOrAttributeName, "get", "is", "set")
    }

    private fun isMethodCall(node: ASTPrimaryExpression): Boolean {
        val suffixes = node.findDescendantsOfType(ASTPrimarySuffix::class.java)
        return suffixes.size == 1 && suffixes[0].isArguments
    }

    private fun isForeignAttributeOrMethod(node: ASTPrimaryExpression): Boolean {
        val result: Boolean
        val nameImage = this.getNameImage(node)

        if (nameImage != null && (!nameImage.contains(".") || nameImage.startsWith("this."))) {
            result = false
        } else if (nameImage == null &&
                node.getFirstDescendantOfType(ASTPrimaryPrefix::class.java).usesThisModifier()) {
            result = false
        } else {
            result = !(nameImage == null &&
                    node.hasDecendantOfAnyType(ASTLiteral::class.java, ASTAllocationExpression::class.java))
        }

        return result
    }

    private fun getNameImage(node: ASTPrimaryExpression): String? {
        val prefix = node.getFirstDescendantOfType(ASTPrimaryPrefix::class.java)
        val name = prefix.getFirstDescendantOfType(ASTName::class.java)

        var image: String? = null
        if (name != null) {
            image = name.image
        }
        return image
    }

    private fun getVariableName(node: ASTPrimaryExpression): String? {
        val prefix = node.getFirstDescendantOfType(ASTPrimaryPrefix::class.java)
        val name = prefix.getFirstDescendantOfType(ASTName::class.java)

        var variableName: String? = null

        if (name != null) {
            val dotIndex = name.image.indexOf(".")
            if (dotIndex == -1) {
                variableName = name.image
            } else {
                variableName = name.image.substring(0, dotIndex)
            }
        }

        return variableName
    }

    private fun getMethodOrAttributeName(node: ASTPrimaryExpression): String? {
        val prefix = node.getFirstDescendantOfType(ASTPrimaryPrefix::class.java)
        val name = prefix.getFirstDescendantOfType(ASTName::class.java)

        var methodOrAttributeName: String? = null

        if (name != null) {
            val dotIndex = name.image.indexOf(".")
            if (dotIndex > -1) {
                methodOrAttributeName = name.image.substring(dotIndex + 1)
            }
        }

        return methodOrAttributeName
    }

    private fun findVariableDeclaration(variableName: String, scope: Scope): VariableNameDeclaration? {
        var result: VariableNameDeclaration? = null

        for (declaration in scope.getDeclarations(VariableNameDeclaration::class.java).keys) {
            if (declaration.image == variableName) {
                result = declaration
                break
            }
        }

        if (result == null && scope.parent != null && scope.parent !is SourceFileScope) {
            result = findVariableDeclaration(variableName, scope.parent)
        }

        return result
    }

    private fun isAttributeAccess(node: ASTPrimaryExpression): Boolean {
        return node.findDescendantsOfType(ASTPrimarySuffix::class.java).isEmpty()
    }

    override fun visit(node: ASTMethodDeclaration, data: Any): Any? {
        this.wmcCounter.increment(this.currentClassScope)

        val currentMethod = node.getFirstChildOfType(ASTMethodDeclarator::class.java).image
        this.methodAttributeAccess[this.currentClassScope]?.put(currentMethod, HashSet<String>())

        this.currentMethodName = currentMethod
        val result = super.visit(node, data)
        this.currentMethodName = null

        return result
    }

    override fun visit(node: ASTConditionalOrExpression, data: Any): Any? {
        this.wmcCounter.increment(this.currentClassScope)
        return super.visit(node, data)
    }

    override fun visit(node: ASTConditionalAndExpression, data: Any): Any? {
        this.wmcCounter.increment(this.currentClassScope)
        return super.visit(node, data)
    }

    override fun visit(node: ASTIfStatement, data: Any): Any? {
        this.wmcCounter.increment(this.currentClassScope)
        return super.visit(node, data)
    }

    override fun visit(node: ASTWhileStatement, data: Any): Any? {
        this.wmcCounter.increment(this.currentClassScope)
        return super.visit(node, data)
    }

    override fun visit(node: ASTForStatement, data: Any): Any? {
        this.wmcCounter.increment(this.currentClassScope)
        return super.visit(node, data)
    }

    override fun visit(node: ASTSwitchLabel, data: Any): Any? {
        this.wmcCounter.increment(this.currentClassScope)
        return super.visit(node, data)
    }

    override fun visit(node: ASTCatchStatement, data: Any): Any? {
        this.wmcCounter.increment(this.currentClassScope)
        return super.visit(node, data)
    }

    override fun visit(node: ASTConditionalExpression, data: Any): Any? {
        if (node.isTernary) {
            this.wmcCounter.increment(this.currentClassScope)
        }
        return super.visit(node, data)
    }

}