package ir.ac.kntu.errors;

public class NoneAllocatingBlock extends Errors {
    static final String MESSAGE = "THIS SIZE WAS NOT ALLOCATING";

    public NoneAllocatingBlock() {
        super(MESSAGE);
    }
}
