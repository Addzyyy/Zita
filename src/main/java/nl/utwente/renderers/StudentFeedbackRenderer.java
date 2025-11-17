package nl.utwente.renderers;

import net.sourceforge.pmd.renderers.AbstractAccumulatingRenderer;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import nl.utwente.processing.LineInFile;
import nl.utwente.processing.ProcessingProject;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/** Renderer that produces student-friendly feedback based on PMD analysis */

public class StudentFeedbackRenderer extends AbstractAccumulatingRenderer {

    private static final Map<String, String> RULE_NAME_OVERRIDES = createRuleNameOverrides();

    private static final Map<String, String> RULE_CATEGORY_MAP = loadCategoryMapping();

    private List<RuleSet> ruleSets;
    private Map<String, List<RuleViolation>> violationsByRule = new HashMap<>();
    private Set<String> rulesWithViolations = new HashSet<>();
    private Boolean buildRuleStatus = null;
    private List<Report> accumulatedReports = new ArrayList<>();
    private ProcessingProject project;

    public StudentFeedbackRenderer() {
        super("test", "Test Renderer");
    }

    public StudentFeedbackRenderer(ProcessingProject project) {
        super("test", "Test Renderer");

        this.project = project;
    }
    // Helper methods to load rule name overrides and category mappings
    private static Map<String, String> createRuleNameOverrides() {
        Map<String, String> overrides = new HashMap<>();
        overrides.put("FieldNamingConventions", "AttributeNamingConventions");
        return overrides;
    }

    // Load rule category mappings from properties file
    private static Map<String, String> loadCategoryMapping() {
        Map<String, String> map = new HashMap<>();
        try (InputStream input = StudentFeedbackRenderer.class.getClassLoader()
                .getResourceAsStream("rule-category-mapping.properties")) {
            if (input == null) {
                System.err.println("Warning: rule-category-mapping.properties not found, rules will be uncategorized");
                return map;
            }
            Properties props = new Properties();
            props.load(input);
            for (String key : props.stringPropertyNames()) {
                String value = props.getProperty(key);
                if (value != null && !value.trim().isEmpty()) {
                    map.put(key.trim(), value.trim());
                }
            }
            System.err.println("Loaded " + map.size() + " rule category mappings");
        } catch (IOException e) {
            System.err.println("Error loading rule category mappings: " + e.getMessage());
        }
        return map;
    }

    public void setRuleSets(List<RuleSet> ruleSets) {
        this.ruleSets = ruleSets;
    }

    public void setBuildRuleStatus(boolean success) {
        this.buildRuleStatus = success;
    }

    public void setProcessingProject(ProcessingProject project) {
        this.project = project;
    }

    @Override
    public String defaultFileExtension() {
        return "txt";
    }

    @Override
    // Render a single file report
    public void renderFileReport(Report report) throws IOException {
        accumulatedReports.add(report);
        for (RuleViolation violation : report.getViolations()) {
            String ruleName = violation.getRule().getName();
            rulesWithViolations.add(ruleName);
            violationsByRule.computeIfAbsent(ruleName, k -> new ArrayList<>()).add(violation);
        }
        super.renderFileReport(report);
    }

    // Map violation line numbers back to Processing code lines
    private String mapViolationToProcessingLine(RuleViolation violation) {
        if (project == null) {
            int lineNumber = violation.getBeginLine();
            if (lineNumber > 0) {
                return "At line " + lineNumber + ": ";
            }
            return "";
        }

        try {
            if (violation.getBeginLine() == 1) {
                return "";
            }

            LineInFile begin = project.mapJavaProjectLineNumber(violation.getBeginLine());
            LineInFile end = project.mapJavaProjectLineNumber(violation.getEndLine());

            if (!begin.getFile().getId().equals(end.getFile().getId())) {
                return "";
            }

            String fileName = begin.getFile().getName();
            int lineNumber = begin.getLine();

            if (lineNumber > 0) {
                return "In " + fileName + " at line " + lineNumber + ": ";
            }

            return "";

        } catch (IndexOutOfBoundsException ex) {
            return "";
        }
    }

    @Override
    // Final rendering of accumulated reports
    // will need to refacor this as the method is too long and complex
    public void end() throws IOException {
        boolean hasParsingErrors = false;
        for (Report report : accumulatedReports) {
            if (report.hasErrors()) {
                hasParsingErrors = true;
                break;
            }
        }

        writer.write("These suggestions were generated automatically by the Zita code quality tool.\n They are designed to help you improve your code, but they may occasionally be incorrect.\n If you're unsure about any suggestion, please ask your TA for clarification.\n\n");

        // Check if build failed by looking for ProgramRunsRule violations
        boolean buildFailed = rulesWithViolations.contains("ProgramRunsRule") ||
                             rulesWithViolations.contains("ProgramRuns");

        // If there were parsing errors, indicate that we cannot evaluate requirements
        if (hasParsingErrors) {
            writer.write("> ProgramRuns: ❌ (syntax/parse error)\n\n");
            writer.write("Cannot evaluate requirements due to syntax errors or an issue with your project.\n");
            writer.write("Please review your submission to make sure it runs and your project is zipped correctly. \n");
            writer.flush();
            return;
        }

        writer.write("========================================\n");
        writer.write("MINIMUM REQUIREMENTS\n");
        writer.write("========================================\n\n");

        Map<String, Map<String, List<RuleResult>>> categoryHierarchy = new LinkedHashMap<>();
        int totalRules = 0;
        int passedRules = 0;
        List<String> criticalFailures = new ArrayList<>();

        if (ruleSets != null && !ruleSets.isEmpty()) {
            for (RuleSet ruleSet : ruleSets) {
                for (Rule rule : ruleSet.getRules()) {
                    String ruleName = rule.getName();

                    // Skip ProgramRunsRule as it's handled separately
                    if (ruleName.equals("ProgramRunsRule") || ruleName.equals("ProgramRuns")) {
                        continue;
                    }
                    totalRules++;
                    boolean passed = !rulesWithViolations.contains(ruleName);
                    if (passed) passedRules++;
                    String status = passed ? "✅" : "❌";
                    String violationMessage = null;
                    if (!passed && violationsByRule.containsKey(ruleName)) {
                        List<RuleViolation> violations = violationsByRule.get(ruleName);
                        if (!violations.isEmpty()) {
                            violationMessage = violations.get(0).getDescription();
                        }
                    }
                    String fullCategory = getCategoryForRule(rule);
                    String mainCategory = "Uncategorized";
                    String subCategory = "General";
                    if (fullCategory != null && fullCategory.contains(".")) {
                        String[] parts = fullCategory.split("\\.");
                        if (parts.length >= 2) {
                            mainCategory = formatCategoryName(parts[0]);
                            StringBuilder subCat = new StringBuilder();
                            for (int i = 1; i < parts.length; i++) {
                                if (subCat.length() > 0) subCat.append(" ");
                                subCat.append(formatCategoryName(parts[i]));
                            }
                            subCategory = subCat.toString();
                        }
                    } else if (fullCategory != null) {
                        mainCategory = formatCategoryName(fullCategory);
                    }
                    if (!passed && mainCategory.toLowerCase().contains("mastery")) {
                        criticalFailures.add(formatRuleName(ruleName) + " [" + subCategory + "]");
                    }
                    categoryHierarchy
                            .computeIfAbsent(mainCategory, k -> new LinkedHashMap<>())
                            .computeIfAbsent(subCategory, k -> new ArrayList<>())
                            .add(new RuleResult(ruleName, status, passed, rule, violationMessage, fullCategory));
                }
            }
        }

        Map<String, Map<String, List<RuleResult>>> minimumCategories = new LinkedHashMap<>();
        Map<String, Map<String, List<RuleResult>>> masteryCategories = new LinkedHashMap<>();

        for (Map.Entry<String, Map<String, List<RuleResult>>> entry : categoryHierarchy.entrySet()) {
            String mainCat = entry.getKey();
            if (mainCat.toLowerCase().contains("minimum")) {
                minimumCategories.put(mainCat, entry.getValue());
            } else if (mainCat.toLowerCase().contains("mastery")) {
                masteryCategories.put(mainCat, entry.getValue());
            }
        }

        for (Map.Entry<String, Map<String, List<RuleResult>>> mainEntry : minimumCategories.entrySet()) {
            for (Map.Entry<String, List<RuleResult>> subEntry : mainEntry.getValue().entrySet()) {
                String subCategory = subEntry.getKey();
                writer.write("-- " + subCategory + " --\n");

                // Output ProgramRuns check first if this is the Submission category
                if (subCategory.equalsIgnoreCase("Submission")) {
                    if (buildFailed) {
                        String buildRuleName = violationsByRule.containsKey("DoesItBuildRule") ? "DoesItBuildRule" :
                                              (violationsByRule.containsKey("ProgramRunsRule") ? "ProgramRunsRule" : "ProgramRuns");

                        if (violationsByRule.containsKey(buildRuleName)) {
                            List<RuleViolation> buildViolations = violationsByRule.get(buildRuleName);
                            if (!buildViolations.isEmpty()) {
                                String violationMessage = buildViolations.get(0).getDescription();
                                writer.write("> ProgramRuns: ❌ " + violationMessage + "\n");
                            }
                        }
                    } else {
                        // Build passed - show it as passing
                        writer.write("> ProgramRuns: ✅\n");
                    }
                }

                for (RuleResult result : subEntry.getValue()) {
                    String displayName = getDisplayRuleName(result.name);
                    writer.write("> " + displayName + ": " + result.status);
                    if (!result.passed) {
                        List<RuleViolation> violations = violationsByRule.get(result.name);
                        if (violations != null && !violations.isEmpty()) {
                            int maxToShow = 3;
                            int count = 0;
                            for (RuleViolation violation : violations) {
                                if (count < maxToShow) {
                                    writer.write("\n");
                                    writer.write("  └─ " + violation.getDescription());
                                    count++;
                                }
                            }
                            if (violations.size() > maxToShow) {
                                writer.write("\n");
                                writer.write("  └─ (and " + (violations.size() - maxToShow) + " more violations)");
                            }
                        }
                    }
                    writer.write("\n");
                }
                writer.write("\n");
            }
        }

        if (!masteryCategories.isEmpty()) {
            boolean hasMasteryViolations = false;
            for (Map.Entry<String, Map<String, List<RuleResult>>> mainEntry : masteryCategories.entrySet()) {
                for (Map.Entry<String, List<RuleResult>> subEntry : mainEntry.getValue().entrySet()) {
                    for (RuleResult result : subEntry.getValue()) {
                        if (!result.passed) {
                            hasMasteryViolations = true;
                            break;
                        }
                    }
                    if (hasMasteryViolations) break;
                }
                if (hasMasteryViolations) break;
            }

            if (hasMasteryViolations) {
                writer.write("=============================================\n");
                writer.write("Additional Design & Code Style improvements \n");
                writer.write("==============================================\n\n");

                for (Map.Entry<String, Map<String, List<RuleResult>>> mainEntry : masteryCategories.entrySet()) {
                    for (Map.Entry<String, List<RuleResult>> subEntry : mainEntry.getValue().entrySet()) {
                        String subCategory = subEntry.getKey();
                        List<RuleResult> failedRules = new ArrayList<>();
                        for (RuleResult result : subEntry.getValue()) {
                            if (!result.passed) {
                                failedRules.add(result);
                            }
                        }
                        if (!failedRules.isEmpty()) {
                            writer.write("-- " + subCategory + " --\n");
                            for (RuleResult result : failedRules) {
                                String displayName = getDisplayRuleName(result.name);
                                writer.write("> " + displayName + ": " + result.status);
                                List<RuleViolation> violations = violationsByRule.get(result.name);
                                if (violations != null && !violations.isEmpty()) {
                                    int maxToShow = 5;
                                    int count = 0;
                                    for (RuleViolation violation : violations) {
                                        if (count < maxToShow) {
                                            writer.write("\n");
                                            String locationPrefix = mapViolationToProcessingLine(violation);
                                            writer.write("  └─ " + locationPrefix + violation.getDescription());
                                            count++;
                                        }
                                    }
                                    if (violations.size() > maxToShow) {
                                        writer.write("\n");
                                        writer.write("  └─ (and " + (violations.size() - maxToShow) + " more violations)");
                                    }
                                }
                                writer.write("\n");
                            }
                            writer.write("\n");
                        }
                    }
                }
            }
        }
        writer.flush();
    }

    // Determine category for a given rule
    private String getCategoryForRule(Rule rule) {
        String ruleName = rule.getName();
        if (RULE_CATEGORY_MAP.containsKey(ruleName)) {
            return RULE_CATEGORY_MAP.get(ruleName);
        }
        String propertyCategory = getRuleProperty(rule, "category");
        if (propertyCategory != null && !propertyCategory.isEmpty()) {
            return propertyCategory;
        }
        return "uncategorized";
    }

    // Helper to get a rule property value as string
    private String getRuleProperty(Rule rule, String propertyName) {
        try {
            PropertyDescriptor<?> descriptor = rule.getPropertyDescriptor(propertyName);
            if (descriptor != null) {
                Object value = rule.getProperty(descriptor);
                return value != null ? value.toString() : null;
            }
        } catch (Exception e) {
        }
        return null;
    }

    // Format category names nicely
    private String formatCategoryName(String category) {
        if (category == null || category.isEmpty()) return "";
        return category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
    }

    // Get display name for a rule, applying overrides if necessary
    private String getDisplayRuleName(String ruleName) {
        // Check if there's an override for this rule name
        if (RULE_NAME_OVERRIDES.containsKey(ruleName)) {
            ruleName = RULE_NAME_OVERRIDES.get(ruleName);
        }
        return formatRuleName(ruleName);
    }

    // Format rule names nicely
    private String formatRuleName(String ruleName) {
        return ruleName.replaceAll("([A-Z])", " $1").trim();
    }

   // Data class to hold rule result information
    private static class RuleResult {
        String name;
        String status;
        boolean passed;
        Rule rule;
        String violationMessage;
        String fullCategory;

        RuleResult(String name, String status, boolean passed, Rule rule, String violationMessage, String fullCategory) {
            this.name = name;
            this.status = status;
            this.passed = passed;
            this.rule = rule;
            this.violationMessage = violationMessage;
            this.fullCategory = fullCategory;
        }
    }
}