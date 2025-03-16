package nl.utwente.processing.pmd.symbols

import java.util.stream.Collectors

/**
 * Simple class which defines a Processing Applet Method.
 */
data class ProcessingAppletMethod(val name: String, val parameters: List<ProcessingAppletParameter>, val category: ProcessingAppletMethodCategory) {

    override fun toString(): String {
        val sb = StringBuilder(name).append('(')
        sb.append(parameters.stream().map { p -> p.toString() }.collect(Collectors.joining(", ")))
        return sb.append(')').toString()
    }
}