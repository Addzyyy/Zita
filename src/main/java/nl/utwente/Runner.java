package nl.utwente;

import net.sourceforge.pmd.renderers.*;
import nl.utwente.processing.pmd.rules.DoesItBuildRule;
import nl.utwente.renderers.AtelierStyleTextRenderer;
import nl.utwente.processing.ProcessingFile;
import nl.utwente.processing.ProcessingProject;
import nl.utwente.processing.pmd.PMDException;
import nl.utwente.processing.pmd.PMDRunner;
import nl.utwente.renderers.StudentFeedbackRenderer;
import nl.utwente.renderers.VivaHandoverRenderer;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class Runner {
    
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

        DoesItBuildRule.Companion.setSketchPath(projectPath);

        var project = new ProcessingProject(
                Files.find(path, 10000,
                        (p, attr) -> attr.isRegularFile() && p.getFileName().toString().endsWith(".pde"))
                        .map(p -> new ProcessingFile(p.getFileName().toString(), p.getFileName().toString(),
                                readString(p)))
                        .collect(Collectors.toList()));

        var runner = new PMDRunner(rulePathStr);
        AbstractIncrementingRenderer renderer = null;
        AbstractAccumulatingRenderer accRenderer = null;
        switch (rendererType.toLowerCase()) {
            case "html":
                renderer = new HTMLRenderer(); 
                break;
            case "json":
                renderer = new JsonRenderer(); 
                break;
            
            case "csv":
            renderer = new CSVRenderer();
            break;

            case "handover":
                accRenderer = new VivaHandoverRenderer();
                break;

            case "student":
                accRenderer = new StudentFeedbackRenderer(project);
                break;
            default:
            renderer = new AtelierStyleTextRenderer(project);
        }

        if (accRenderer instanceof StudentFeedbackRenderer) {
            ((StudentFeedbackRenderer) accRenderer).setRuleSets(runner.getRuleSets());
        }
        if (accRenderer instanceof VivaHandoverRenderer) {
            ((VivaHandoverRenderer) accRenderer).setRuleSets(runner.getRuleSets());
        }
        if (accRenderer != null) {
            accRenderer.setWriter(new PrintWriter(System.out));
            runner.Run(project, accRenderer);
            DoesItBuildRule.Companion.resetRunFlag();
        } else {

           renderer.setWriter(new PrintWriter(System.out));
           runner.Run(project, renderer);
        }


        }
        }


