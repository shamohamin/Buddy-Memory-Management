package ir.ac.kntu.errors;

public class ExceedMemorySizeError extends Errors {
    private static final String MESSAGE = "Memory Doesn't Have Enough Space.";

    public ExceedMemorySizeError() {
        super(MESSAGE);
    }
}
