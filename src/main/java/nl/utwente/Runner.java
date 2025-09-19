package nl.utwente;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer;
import net.sourceforge.pmd.renderers.HTMLRenderer;
import net.sourceforge.pmd.renderers.JsonRenderer;
import nl.utwente.processing.LineInFile;
import nl.utwente.processing.ProcessingFile;
import nl.utwente.processing.ProcessingProject;
import nl.utwente.processing.pmd.PMDException;
import nl.utwente.processing.pmd.PMDRunner;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Runner {
    // I'm sorry, but this is only for development purposes
    static String readString(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException ex) {
            // Print the error message to the screen
            System.out.println("Error reading file: " + path);
            System.out.println("Exception thrown reading files.");
            return "";
        }
    }

    static class AtelierStyleTextRenderer extends AbstractIncrementingRenderer {
        private final ProcessingProject project;

        public AtelierStyleTextRenderer(ProcessingProject project) {
            super("AtelierStyleText", "Renders the output as text, using a style like comments on Atelier");
            this.project = project;
        }

        @Override
        public String defaultFileExtension() {
            return "";
        }

        private String padLeft(String input, int length) {
            return " ".repeat(length - input.length()) + input;
        }

        @Override
        public void renderFileViolations(Iterator<RuleViolation> violations) {
            LinkedList<String> liViolations = new LinkedList<>();
            Map<String, Integer> mViolations = new HashMap<String, Integer>();

            System.out.println(
                    "These suggestions were generated automatically by the Zita code quality tool. They are designed to help you improve your code, but they may occasionally be incorrect. If you’re unsure about any suggestion, please ask your TA for clarification.\n");
            /* Generate Comments for Violations Found */
            while (violations.hasNext()) {
                var violation = violations.next();

                LineInFile begin, end;
                try {

                    if (violation.getBeginLine() != 1) {
                        begin = project.mapJavaProjectLineNumber(violation.getBeginLine());
                        end = project.mapJavaProjectLineNumber(violation.getEndLine());
                    } else {
                        begin = new LineInFile(1,
                                new ProcessingFile(violation.getDescription(), violation.getFilename(), ""));
                        end = new LineInFile(1,
                                new ProcessingFile(violation.getDescription(), violation.getFilename(), ""));
                    }

                    if (!begin.getFile().getId().equals(end.getFile().getId())) {
                        System.out.println("! Dismissing violation of " + violation.getRule().getName()
                                + ": Line numbers are not in the same source file\n");
                        continue;
                    }

                } catch (IndexOutOfBoundsException ex) {
                    System.out.println("! Dismissing violation of " + violation.getRule().getName()
                            + ": Line number is not in a source file\n");
                    continue;
                }

                String sRuleName = mAddSpacesToString(violation.getRule().getName()).trim();
                if (mViolations.containsKey(sRuleName)) {
                    mViolations.replace(sRuleName, mViolations.get(sRuleName) + 1);
                } else {
                    mViolations.put(sRuleName, 1);
                }

                var lineStart = begin.getLine();
                var charStart = violation.getBeginColumn();
                var lineEnd = end.getLine();
                var charEnd = violation.getEndColumn();

                if (lineStart == -1 || lineEnd == -1) {
                    StringBuilder sbViolationMessage = new StringBuilder();
                    sbViolationMessage.append("> ");
                    sbViolationMessage.append(violation.getDescription()).append("\n");
                    liViolations.addLast(sbViolationMessage.toString());
                    continue;

                }

                if (lineStart == lineEnd && charStart == charEnd) {
                    var lines = begin.getFile().getContent().lines().collect(Collectors.toList());
                    String line;
                    if (lineStart > 0 && lineStart <= lines.size()) {
                        line = lines.get(lineStart - 1);
                    } else {
                        line = "[Line unavailable]";
                        System.err.println("⚠️ Warning: Requested line " + lineStart
                                + " is out of bounds. Total lines: " + lines.size());
                    }

                    charStart = line.indexOf(line.trim());
                    charEnd = line.length();
                } else {
                    charStart = Math.max(0, charStart - 1);
                }
                String fileName = begin.getFile().getName();
                StringBuilder sbViolationMessage = new StringBuilder();

                sbViolationMessage
                        .append("> In file ")
                        .append(fileName)
                        .append(" at line ")
                        .append(lineStart)
                        .append(": ")
                        .append(violation.getDescription())
                        .append("\n");

                liViolations.addLast(sbViolationMessage.toString());
            }

            /* Generate Summary Comment */
            // StringBuilder sbSummaryMessage = new StringBuilder("These warnings were
            // created by the Zita code quality tool.\nThey are advisory and may be
            // sometimes incorrect.\nSee if they make sense to you, and consult the TA if
            // needed:\n");

            // liViolations.addFirst(sbSummaryMessage.toString());

            /* Print all comments */
            for (String sMsg : liViolations) {
                System.out.println(sMsg);
            }
        }

        @Override
        public void end() {
            for (var err : errors) {
                if (err.getMsg().contains("Processing.pde")) {
                    System.out.println("Error during program load, ZITA could not properly read the program files.");
                } else {
                    System.out.println("Error during program load, ZITA could not properly read the program files");
                }
            }
        }

        public String mAddSpacesToString(String sWord) {
            if (sWord.length() == 0) {
                return "";
            }
            return ((Character.isUpperCase(sWord.charAt(0)) ? " " : "") + sWord.charAt(0)
                    + mAddSpacesToString(sWord.substring(1)));
        }
    }

    public static void main(String[] args) throws IOException, PMDException {

        String projectPath = null;
        String rulePath = null;
        String rendererType = "zita";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--project":
                    if (i + 1 < args.length)
                        projectPath = args[++i];
                    break;
                case "--rules":
                    if (i + 1 < args.length)
                        rulePath = args[++i];
                    break;
                case "--renderer":
                    if (i + 1 < args.length)
                        rendererType = args[++i];
                    break;
            }
        }
        if (projectPath == null || rulePath == null) {
            System.out.println("Usage: --project <project path> --rules <rule path> [--renderer <type>]");
            return;
        }

        var path = Path.of(projectPath);
        var rulePathStr = Path.of(rulePath).toString();
        var project = new ProcessingProject(
                Files.find(path, 10000,
                        (p, attr) -> attr.isRegularFile() && p.getFileName().toString().endsWith(".pde"))
                        .map(p -> new ProcessingFile(p.getFileName().toString(), p.getFileName().toString(),
                                readString(p)))
                        .collect(Collectors.toList()));

        var runner = new PMDRunner(rulePathStr);
        AbstractIncrementingRenderer renderer;
        switch (rendererType.toLowerCase()) {
            case "html":
                renderer = new HTMLRenderer(); // implement or import this
                break;
            case "json":
                renderer = new JsonRenderer(); // implement or import this
                break;
            default:
                renderer = new AtelierStyleTextRenderer(project);
        }
        renderer.setWriter(new PrintWriter(System.out));
        runner.Run(project, renderer);
    }
}
