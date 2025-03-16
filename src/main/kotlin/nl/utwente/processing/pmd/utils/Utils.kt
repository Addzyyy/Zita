package nl.utwente.processing.pmd.utils

import net.sourceforge.pmd.lang.java.ast.ASTMethodDeclaration
import net.sourceforge.pmd.lang.java.symboltable.MethodNameDeclaration
import net.sourceforge.pmd.lang.symboltable.NameOccurrence

fun <K, V> Map<K, Collection<V>>.keysByValue(value: V?) : Set<K> {
    if (value == null) return emptySet()
    val result = HashSet<K>();
    for ((key, set) in this) {
        if (value in set) {
            result.add(key)
        }
    }
    return result
}

fun <K> MutableMap<K, Int>.increment(key: K?) : Int {
    if (key == null) return 0
    val oldValue = this[key] ?: 0
    this.put(key, oldValue + 1)
    return oldValue
}

fun Map<MethodNameDeclaration, Collection<NameOccurrence>>.mapKeysToNodes() :
        Map<ASTMethodDeclaration, Collection<NameOccurrence>> {
    val result = HashMap<ASTMethodDeclaration, Collection<NameOccurrence>>()
    this.entries.stream()
            .filter { (k, _) -> k.methodNameDeclaratorNode?.getFirstParentOfType(
                    ASTMethodDeclaration::class.java) != null }
            .forEach { (k, v) -> result.put(k.methodNameDeclaratorNode.getFirstParentOfType(
                    ASTMethodDeclaration::class.java), v) }
    return result
}