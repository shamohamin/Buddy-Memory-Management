package ir.ac.kntu.os;

import ir.ac.kntu.errors.MaximumAllocationError;

public interface IMemoryManager {
    int MAX_MEMORY_SIZE = 8192;
    int MIN_MEMORY_SZIE = 2048;
    void deAllocation(int pid, long address);
    long allocation(int pid, int size);
    void setMemorySize(int size) throws MaximumAllocationError;
    int getMemorySize();
}
