package nl.utwente.processing.pmd.rules

import net.sourceforge.pmd.lang.java.ast.ASTCompilationUnit
import net.sourceforge.pmd.lang.java.rule.AbstractJavaRule
import net.sourceforge.pmd.RuleContext
import net.sourceforge.pmd.properties.PropertyDescriptor
import net.sourceforge.pmd.properties.PropertyFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * Rule that checks whether the Processing sketch builds successfully using processing-java.
 * If the build fails, a violation is reported.
 */
class DoesItBuildRule: AbstractJavaRule() {



    companion object {
        private val CATEGORY: PropertyDescriptor<String> =
            PropertyFactory.stringProperty("category")
                .desc("Rule category")
                .defaultValue("default")
                .build()
        private var hasRun = false
        var sketchPath: String? = null

        // Reset for testing
        fun resetRunFlag() {
            hasRun = false
            sketchPath = null
        }
    }
    init {
        definePropertyDescriptor(CATEGORY)
    }
    override fun visit(node: ASTCompilationUnit, data: Any?): Any? {
        val ctx = data as RuleContext

        // Only run once per PMD execution
        if (!hasRun && shouldRunBuild(ctx)) {
            hasRun = true

            val result = buildSketch(ctx)

            if (!result.success) {
                addViolationWithMessage(
                    data, node,
                    "Processing sketch failed to build: ${result.errorMessage}"
                )
            }
        }

        return super.visit(node, data)
    }

    private fun shouldRunBuild(ctx: RuleContext): Boolean {
        val filename = ctx.sourceCodeFilename
        return filename != null &&
                (filename.endsWith("Processing.pde") || filename.contains(".pde"))
    }

    private fun buildSketch(ctx: RuleContext): BuildResult {
        val result = BuildResult()

        try {
            val path = sketchPath ?: throw IllegalStateException("Sketch path is null")

            // Use --build to just compile, not run
            val pb = ProcessBuilder(
                "processing-java",
                "--sketch=$path",
                "--build"
            )

            pb.redirectErrorStream(true)
            val process = pb.start()

            // Capture output
            val output = StringBuilder()
            val reader = BufferedReader(InputStreamReader(process.inputStream))

            reader.useLines { lines ->
                lines.forEach { line ->
                    output.append(line).append("\n")
                }
            }

            // Wait for build to complete
            val finished = process.waitFor(30, TimeUnit.SECONDS)

            if (!finished) {
                process.destroy()
                result.success = false
                result.errorMessage = "Build timeout after 30 seconds"
                return result
            }

            val exitCode = process.exitValue()
            result.success = (exitCode == 0)
            result.output = output.toString()

            if (result.output.contains("Finished")) {

                result.success = true
                result.output = output.toString();
                return result
            } else if (result.output.contains("Not a valid sketch folder")) {

                result.errorMessage = """
                    |Your submission did NOT run successfully.
                    |   - Please ensure your program runs on your machine and is free of errors. 
                    |   - Please ensure you only upload a single zip file.
                    |   - The zip file should contain a single folder.
                    |   - The single folder should contain all your Processing files for your project.
                    |   - The name of the folder should match the name of your main Processing file/tab that contains your draw and setup functions.
                    |   - The simplest way to achieve this is to right-click on your project folder that contains your Processing files and zip/compress that folder.
                    |   - Please reupload your submission and check if we can build it.
                """.trimMargin()
                return result;
            }


            if (!result.success) {
                result.errorMessage = extractErrorMessages(result.output)
            }

        } catch (e: Exception) {
            result.success = false
            result.errorMessage = "Exception during build: ${e.message}"
        }

        return result
    }

    private fun extractErrorMessages(output: String): String {
        // Extract just the error lines for cleaner output
        val errors = StringBuilder()
        val lines = output.split("\n")

        for (line in lines) {
            val lower = line.toLowerCase();
            if ("error" in lower || "exception" in lower ||
                "cannot find symbol" in lower || "expected" in lower
            ) {
                errors.append(line).append("\n")
            }
        }

        return if (errors.isNotEmpty()) errors.toString() else output
    }

    private data class BuildResult(
        var success: Boolean = false,
        var errorMessage: String = "",
        var output: String = ""
    )

}