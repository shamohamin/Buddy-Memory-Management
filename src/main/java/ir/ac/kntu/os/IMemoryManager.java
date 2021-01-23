package ir.ac.kntu.os;

import ir.ac.kntu.errors.MaximumAllocationError;
import ir.ac.kntu.errors.NoneDellocatingBlockError;

public interface IMemoryManager {
    int MAX_MEMORY_SIZE = 8192;
    int MIN_MEMORY_SZIE = 2048;
    int MIN_BLOCK_SIZE = 32;
    int MAX_BLOCK_SIZE = 1024;
    void deAllocation(int pid, long address) throws NoneDellocatingBlockError;
    long allocation(int pid, int size);
    void setMemorySize(int size) throws MaximumAllocationError;
    int getMemorySize();
}
