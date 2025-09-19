package nl.utwente.renderers;

import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.renderers.AbstractIncrementingRenderer;
import nl.utwente.processing.LineInFile;
import nl.utwente.processing.ProcessingFile;
import nl.utwente.processing.ProcessingProject;
import java.util.*;
import java.util.stream.Collectors;

public class AtelierStyleTextRenderer extends AbstractIncrementingRenderer {
    private final ProcessingProject project;

    public AtelierStyleTextRenderer(ProcessingProject project) {
        super("AtelierStyleText", "Renders the output as text, using a style like comments on Atelier");
        this.project = project;
    }

    @Override
    public String defaultFileExtension() {
        return "";
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
