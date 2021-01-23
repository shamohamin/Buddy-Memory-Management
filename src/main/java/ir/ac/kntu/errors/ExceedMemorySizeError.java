package ir.ac.kntu.errors;

public class ExceedMemorySizeError extends Errors {
    private static final String MESSAGE = "Exceeds Memory Maximum Block Size which is 1024MB";

    public ExceedMemorySizeError() {
        super(MESSAGE);
    }
}
