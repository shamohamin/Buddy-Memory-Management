package ir.ac.kntu.process;

public interface IProcess {
    void deAllocation(int pid, long address);
    long allocation(int pid, int size);
}
