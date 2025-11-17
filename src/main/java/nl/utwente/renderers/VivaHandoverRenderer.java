package nl.utwente.renderers;

import net.sourceforge.pmd.renderers.AbstractAccumulatingRenderer;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.Report;
import java.io.IOException;
import java.util.*;

/**
 * Renderer that outputs a simple JSON structure to stdout for automated marking.
 * Format: {"rules": {"RuleName": "pass|fail|N/A", ...}}
 */
public class VivaHandoverRenderer extends AbstractAccumulatingRenderer {

    private List<RuleSet> ruleSets;
    private Set<String> rulesWithViolations = new HashSet<>();
    private Boolean buildRuleStatus = null;
    private List<Report> accumulatedReports = new ArrayList<>();

    public VivaHandoverRenderer() {
        super("marking", "Marking Renderer for CSV Export");
    }

    public void setRuleSets(List<RuleSet> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public void setBuildRuleStatus(boolean success) {
        this.buildRuleStatus = success;
    }

    @Override
    public String defaultFileExtension() {
        return "json";
    }

    @Override
    // Render a single file report
    public void renderFileReport(Report report) throws IOException {
        accumulatedReports.add(report);

        for (RuleViolation violation : report.getViolations()) {
            String ruleName = violation.getRule().getName();
            rulesWithViolations.add(ruleName);
        }

        super.renderFileReport(report);
    }

    @Override
    // Final rendering of accumulated reports
    public void end() throws IOException {
        // Check for parsing errors
        boolean hasParsingErrors = false;
        for (Report report : accumulatedReports) {
            if (report.hasErrors()) {
                hasParsingErrors = true;
                break;
            }
        }

        // Determine if we should mark all rules as N/A (only on parsing errors, not build failures)
        boolean shouldMarkAllNA = hasParsingErrors;

        Map<String, String> results = new LinkedHashMap<>();

        // Process all rules
        if (ruleSets != null && !ruleSets.isEmpty()) {
            for (RuleSet ruleSet : ruleSets) {
                for (Rule rule : ruleSet.getRules()) {
                    String ruleName = rule.getName();

                    // Handle DoesItBuildRule and ProgramRunsRule separately
                    if (ruleName.equals("ProgramRunsRule") || ruleName.equals("DoesItBuildRule")) {
                        if (hasParsingErrors) {
                            // Only report violation if there's a parsing error
                            results.put(ruleName, "fail");
                        } else {
                            // No parsing errors means build succeeded, always pass
                            results.put(ruleName, "pass");
                        }
                    } else {
                        // All other rules: mark as N/A if build failed or parsing errors
                        if (shouldMarkAllNA) {
                            results.put(ruleName, "N/A");
                        } else {
                            boolean passed = !rulesWithViolations.contains(ruleName);
                            results.put(ruleName, passed ? "pass" : "fail");
                        }
                    }
                }
            }
        }

        writer.write("{\n");
        writer.write("  \"rules\": {\n");

        int count = 0;
        int total = results.size();

        for (Map.Entry<String, String> entry : results.entrySet()) {
            count++;
            writer.write("    \"" + escapeJson(entry.getKey()) + "\": \"" + entry.getValue() + "\"");
            if (count < total) {
                writer.write(",");
            }
            writer.write("\n");
        }

        writer.write("  }\n");
        writer.write("}\n");

        writer.flush();
    }

    // Simple JSON string escaper
    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}