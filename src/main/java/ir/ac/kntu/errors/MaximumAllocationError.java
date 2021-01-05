package ir.ac.kntu.errors;

public class MaximumAllocationError extends Errors {
    private static final String MESSAGE = "Memory Doesn't Have Enough Space.";

    public MaximumAllocationError() {
        super(MESSAGE);
    }
}
