package nl.utwente.renderers;

import net.sourceforge.pmd.renderers.AbstractAccumulatingRenderer;
import net.sourceforge.pmd.RuleViolation;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TestRenderer extends AbstractAccumulatingRenderer {

    private static final Map<String, String> RULE_CATEGORY_MAP = loadCategoryMapping();

    private List<RuleSet> ruleSets;
    private Map<String, List<RuleViolation>> violationsByRule = new HashMap<>();
    private Set<String> rulesWithViolations = new HashSet<>();
    private Boolean buildRuleStatus = null;
    private List<Report> accumulatedReports = new ArrayList<>();

    public TestRenderer() {
        super("test", "Test Renderer");
    }

    /**
     * Load rule category mappings from properties file
     */
    private static Map<String, String> loadCategoryMapping() {
        Map<String, String> map = new HashMap<>();

        try (InputStream input = TestRenderer.class.getClassLoader()
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

    @Override
    public String defaultFileExtension() {
        return "txt";
    }

    @Override
    public void renderFileReport(Report report) throws IOException {
        accumulatedReports.add(report);

        for (RuleViolation violation : report.getViolations()) {
            String ruleName = violation.getRule().getName();
            rulesWithViolations.add(ruleName);
            violationsByRule.computeIfAbsent(ruleName, k -> new ArrayList<>()).add(violation);
        }

        super.renderFileReport(report);
    }

    @Override
    public void end() throws IOException {
        // Check for parsing errors
        boolean hasParsingErrors = false;
        for (Report report : accumulatedReports) {
            if (report.hasErrors()) {
                hasParsingErrors = true;
                break;
            }
        }

        writer.write("========================================\n");
        writer.write("SUBMISSION FEEDBACK\n");
        writer.write("========================================\n\n");

        // Build status
        if (hasParsingErrors) {
            writer.write("> Build Status: Fail ✗ (syntax/parse error)\n\n");
            writer.write("Cannot evaluate requirements due to syntax errors.\n");
            writer.write("Please fix compilation errors first.\n");
            writer.flush();
            return;
        } else if (buildRuleStatus != null) {
            String status = buildRuleStatus ? "Pass ✓" : "Fail ✗";
            writer.write("> Build Status: " + status + "\n\n");
        }

        writer.write("========================================\n");
        writer.write("MINIMUM REQUIREMENTS\n");
        writer.write("========================================\n\n");

        // Group rules by category hierarchy
        Map<String, Map<String, List<RuleResult>>> categoryHierarchy = new LinkedHashMap<>();
        int totalRules = 0;
        int passedRules = 0;
        List<String> criticalFailures = new ArrayList<>();

        if (ruleSets != null && !ruleSets.isEmpty()) {
            for (RuleSet ruleSet : ruleSets) {
                for (Rule rule : ruleSet.getRules()) {
                    String ruleName = rule.getName();

                    if (ruleName.equals("DoesItBuildRule")) {
                        continue;
                    }

                    totalRules++;
                    boolean passed = !rulesWithViolations.contains(ruleName);
                    if (passed) passedRules++;

                    String status = passed ? "✅" : "❌";

                    // Get the violation message for failed rules
                    String violationMessage = null;
                    if (!passed && violationsByRule.containsKey(ruleName)) {
                        List<RuleViolation> violations = violationsByRule.get(ruleName);
                        if (!violations.isEmpty()) {
                            violationMessage = violations.get(0).getDescription();
                        }
                    }

                    // Get category from mapping
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

                    // Track critical failures (mastery requirements)
                    if (!passed && mainCategory.toLowerCase().contains("mastery")) {
                        criticalFailures.add(formatRuleName(ruleName) + " [" + subCategory + "]");
                    }

                    // Add to hierarchy with violation message
                    categoryHierarchy
                            .computeIfAbsent(mainCategory, k -> new LinkedHashMap<>())
                            .computeIfAbsent(subCategory, k -> new ArrayList<>())
                            .add(new RuleResult(ruleName, status, passed, rule, violationMessage));
                }
            }
        }

        // Separate minimum and mastery categories
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

        // Output minimum requirements with messages
        for (Map.Entry<String, Map<String, List<RuleResult>>> mainEntry : minimumCategories.entrySet()) {
            for (Map.Entry<String, List<RuleResult>> subEntry : mainEntry.getValue().entrySet()) {
                String subCategory = subEntry.getKey();
                writer.write("-- " + subCategory + " --\n");

                for (RuleResult result : subEntry.getValue()) {
                    String displayName = formatRuleName(result.name);
                    writer.write("> " + displayName + ": " + result.status);

                    // Add violation messages below for failed rules (show up to 3)
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

                            // Show count if there are more
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

        // Output mastery requirements with messages
        if (!masteryCategories.isEmpty()) {
            writer.write("========================================\n");
            writer.write("MASTERY REQUIREMENTS\n");
            writer.write("========================================\n\n");

            for (Map.Entry<String, Map<String, List<RuleResult>>> mainEntry : masteryCategories.entrySet()) {
                for (Map.Entry<String, List<RuleResult>> subEntry : mainEntry.getValue().entrySet()) {
                    String subCategory = subEntry.getKey();
                    writer.write("-- " + subCategory + " --\n");

                    for (RuleResult result : subEntry.getValue()) {
                        String displayName = formatRuleName(result.name);
                        writer.write("> " + displayName + ": " + result.status);

                        // Add violation messages below for failed rules (show up to 3)
                        if (!result.passed) {
                            List<RuleViolation> violations = violationsByRule.get(result.name);

                            if (violations != null && !violations.isEmpty()) {
                                int maxToShow = 5;
                                int count = 0;

                                for (RuleViolation violation : violations) {
                                    if (count < maxToShow) {
                                        writer.write("\n");
                                        writer.write("  └─ " + violation.getDescription());
                                        count++;
                                    }
                                }

                                // Show count if there are more
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
        }



        writer.flush();
    }

    /**
     * Get category for a rule from the properties mapping
     */
    private String getCategoryForRule(Rule rule) {
        String ruleName = rule.getName();

        // Check properties file mapping
        if (RULE_CATEGORY_MAP.containsKey(ruleName)) {
            return RULE_CATEGORY_MAP.get(ruleName);
        }

        // Fallback: check if rule has category property
        String propertyCategory = getRuleProperty(rule, "category");
        if (propertyCategory != null && !propertyCategory.isEmpty()) {
            return propertyCategory;
        }

        return "uncategorized";
    }

    private String getRuleProperty(Rule rule, String propertyName) {
        try {
            PropertyDescriptor<?> descriptor = rule.getPropertyDescriptor(propertyName);
            if (descriptor != null) {
                Object value = rule.getProperty(descriptor);
                return value != null ? value.toString() : null;
            }
        } catch (Exception e) {
            // Property doesn't exist
        }
        return null;
    }

    private String formatCategoryName(String category) {
        // Convert "minimum" or "constructs" to "Minimum" or "Constructs"
        if (category == null || category.isEmpty()) return "";
        return category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
    }

    private String formatRuleName(String ruleName) {
        // Convert "HasUserDefinedMethod" to "Has User Defined Method"
        return ruleName.replaceAll("([A-Z])", " $1").trim();
    }

    private static class RuleResult {
        String name;
        String status;
        boolean passed;
        Rule rule;
        String violationMessage;

        RuleResult(String name, String status, boolean passed, Rule rule, String violationMessage) {
            this.name = name;
            this.status = status;
            this.passed = passed;
            this.rule = rule;
            this.violationMessage = violationMessage;
        }
    }
}