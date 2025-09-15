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

/** Wrapper around PMD that allows for easy processing of projects */
public class PMDRunner {

    // PMD Docs: https://pmd.github.io/pmd-6.27.0/pmd_userdocs_tools_java_api.html

    private PMDConfiguration config;
    private RuleSetFactory ruleSetFactory;
    private ArrayList<DataSource> datasources = new ArrayList<DataSource>();


    public PMDRunner() {

        this("rulesets/atelier.xml");
    }

    public PMDRunner(String ruleSets) {
        config = new PMDConfiguration();
        config.setMinimumPriority(RulePriority.LOW);
        config.setRuleSets(ruleSets);
        config.setIgnoreIncrementalAnalysis(true);
        ruleSetFactory = RulesetsFactoryUtils.createFactory(config);
    }


    /** Run a list of files through PMD, sending the results to the provided renderer */
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