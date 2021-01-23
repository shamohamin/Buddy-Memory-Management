package ir.ac.kntu.errors;

public class NoneDellocatingBlockError extends Errors {
    static final String MESSAGE = "THIS DEALLOCATED BLOCK WAS NOT IN THIS PROCESS.";

    public NoneDellocatingBlockError() {
        super(MESSAGE);
    }
}
