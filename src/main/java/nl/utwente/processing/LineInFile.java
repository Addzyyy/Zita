package nl.utwente.processing;

/** A line in a file. This exists, because Java. */
public class LineInFile {
    private final int line;
    private final ProcessingFile file;

    public LineInFile(final int line, final ProcessingFile file) {
        this.line = line;
        this.file = file;
    }

    public int getLine() {
        return line;
    }

    public ProcessingFile getFile() {
        return file;
    }
}