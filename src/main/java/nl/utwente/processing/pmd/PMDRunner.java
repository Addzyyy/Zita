package nl.utwente.processing.pmd;

import net.sourceforge.pmd.*;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.util.ClasspathClassLoader;
import net.sourceforge.pmd.util.datasource.DataSource;
import net.sourceforge.pmd.util.datasource.ReaderDataSource;
import nl.utwente.processing.ProcessingProject;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
/** Wrapper around PMD that allows for easy processing of projects */
public class PMDRunner {

    // PMD Docs: https://pmd.github.io/pmd-6.27.0/pmd_userdocs_tools_java_api.html

    private PMDConfiguration config;
    private RuleSetFactory ruleSetFactory;

    private RuleSets ruleSets;
    private ArrayList<DataSource> datasources = new ArrayList<DataSource>();


    public PMDRunner() {

        this("rulesets/atelier.xml");
    }

    public PMDRunner(String ruleSets) {

        Logger.getLogger("net.sourceforge.pmd").setLevel(Level.SEVERE);
        config = new PMDConfiguration();
        config.setMinimumPriority(RulePriority.LOW);
        config.setRuleSets(ruleSets);
        config.setIgnoreIncrementalAnalysis(true);
        ruleSetFactory = RulesetsFactoryUtils.createFactory(config);

        try {
            this.ruleSets = ruleSetFactory.createRuleSets(config.getRuleSets());

        } catch (RuleSetNotFoundException e) {

            throw new RuntimeException("Ruleset not found: " + e.getMessage());
        }
    }

    public List<RuleSet> getRuleSets() {

        if (ruleSets != null) {

            List<RuleSet> ruleSetList = new ArrayList<RuleSet>();

            Collections.addAll(ruleSetList, ruleSets.getAllRuleSets());

            return ruleSetList;
        }
        return Collections.emptyList();
    }/** Run a list of files through PMD, sending the results to the provided renderer */
    public void Run(ProcessingProject project, Renderer renderer) throws PMDException {
        try {
            renderer.start();

            List<DataSource> datasources = Collections.singletonList(
                    new ReaderDataSource(new StringReader(project.getJavaProjectCode()), "Processing.pde")
            );

            try {
                PMD.processFiles(
                        config,
                        ruleSetFactory,
                        datasources,
                        new RuleContext(),
                        Collections.singletonList(renderer)
                );
            } finally {
                ClassLoader auxiliaryClassLoader = config.getClassLoader();
                if (auxiliaryClassLoader instanceof ClasspathClassLoader) {
                    ((ClasspathClassLoader) auxiliaryClassLoader).close();
                }
            }

            renderer.end();
            renderer.flush();
        } catch (Exception e) {
            throw new PMDException(e);
        }
    }
}