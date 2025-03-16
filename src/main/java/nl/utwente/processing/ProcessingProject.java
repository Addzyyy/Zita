package nl.utwente.processing;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    public List<String> getFileNames() {
        ArrayList<String> retval = new ArrayList<String>();
       

        for (ProcessingFile file : files) {
            retval.add(file.getName());
        }

        return retval;
    }

    /** Roughly convert Processing code to Java code */
    private String toJava(String code, String fileName) {
        code = START_JAVA_CODE + code + END_JAVA_CODE;
        code = code.replaceAll("\\bint\\s*\\(", "toInt(");
        code = code.replaceAll("\\bfloat\\s*\\(", "toFloat(");
        code = code.replace(" = #", " = 0x");
        code = code.replace("(#", "(0x");
        code = code.replaceAll("import(.)*;", "");
        return code;
    }

    public List<String> getProjectCodeIndividualFiles() {

        ArrayList<String> retval = new ArrayList<String>();
       

        for (ProcessingFile file : files) {
            retval.add(file.getContent());
        }

        return retval;
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
        return toJava(getProjectCode(), "Processing");
    }
    public List<String> getJavaProjectCodeIndividualFiles() {

        ArrayList<String> retval = new ArrayList<String>();
       

        for (int i = 0; i < files.size(); i++) {
            retval.add(toJava(files.get(i).getContent(), files.get(i).getName()));
        }
        
        return retval;
    }

    /** Map line in Java code back to Processing file and line */
    public LineInFile mapJavaProjectLineNumber(int line) {
        var projectLine = line - (int)countChars('\n', START_JAVA_CODE);
        return mapProjectLineNumber(projectLine);
    }
}