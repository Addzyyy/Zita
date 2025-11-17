package nl.utwente.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


// "hacks to make  processing conversion work for java parsing"
// https://github.com/swordiemen/zita/blob/master/src/main/java/nl/utwente/zita/parsing/Parser.java

/** Helper class to deal with Processing code in PMD */
public class ProcessingProject {
    private static String START_JAVA_CODE = "public class Processing {\r\n";
    private static String END_JAVA_CODE = "\r\n}";

    private List<ProcessingFile> files;
    
    public ProcessingProject(List<ProcessingFile> files) {
        this.files = files;
    }

    private long countChars(char ch, String str) {
        return str.chars().filter(x -> x == ch).count();
    }

    /** Roughly convert Processing code to Java code */
    private String toJava(String code) {
        // Extract all import statements
        Pattern importPattern = Pattern.compile("import\\s+.*?;");
        Matcher matcher = importPattern.matcher(code);
        StringBuilder imports = new StringBuilder();

        while (matcher.find()) {
            imports.append(matcher.group()).append("\n");
        }

        // Remove imports from original code
        code = matcher.replaceAll("");

        // Build final code with imports first
        code = imports.toString() + START_JAVA_CODE + code + END_JAVA_CODE;
        code = code.replaceAll("\\bint\\s*\\(", "toInt(");
        code = code.replaceAll("\\bfloat\\s*\\(", "toFloat(");
        code = code.replace(" = #", " = 0x");
        code = code.replace("(#", "(0x");
        code = code.replaceAll(":\\s*#", ": 0x");
        return code;
    }


    /** Combine all Processing files into a single string */
    public String getProjectCode() {
        return files.stream().map(file -> file.getContent()).collect(Collectors.joining("\n"));
    }

    /** Find the file and line number from the combined project string */
    public LineInFile mapProjectLineNumber(int line) {
        var l = line;
        if (l == 0)
            throw new IndexOutOfBoundsException(l);
        for (int i = 0; i < files.size(); i++) {
            var fileSize = countChars('\n', files.get(i).getContent()) + 1;
            if (fileSize >= l) {
                return new LineInFile(l, files.get(i));
            } else {
                l -= fileSize;
            }
        }

        throw new IndexOutOfBoundsException(line);
    }

    /** Get the full project code converted to Java */
    public String getJavaProjectCode() {
        return toJava(getProjectCode());
    }

    /** Map line in Java code back to Processing file and line */
    public LineInFile mapJavaProjectLineNumber(int line) {
        var projectLine = line - (int)countChars('\n', START_JAVA_CODE);
        return mapProjectLineNumber(projectLine);
    }
}