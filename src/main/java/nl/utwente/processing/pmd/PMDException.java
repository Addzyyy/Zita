package nl.utwente.processing.pmd;

/** Something went wrong while running PMD */
public class PMDException extends Exception {
    public PMDException(Exception ex) {
        super(ex);
    }
}
