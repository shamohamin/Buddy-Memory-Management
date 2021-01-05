package ir.ac.kntu.os;

import ir.ac.kntu.errors.MaximumAllocationError;

import java.util.concurrent.locks.ReentrantLock;

/**
 * OsMemoryManager this class implements the buddy algorithm for Memory Allocation
 */
public class OsMemoryManager implements IMemoryManager {
    private ReentrantLock lock;
    private int maximumMemorySize;
    private static OsMemoryManager instance;

    private OsMemoryManager(int maximumMemorySize) {
        this.lock = new ReentrantLock();
        this.maximumMemorySize = maximumMemorySize;
    }

    public static OsMemoryManager getInstance() {
        if (instance == null){
            instance = new OsMemoryManager(MIN_MEMORY_SZIE);
        }
        return instance;
    }

    public void setMemorySize(int maximumMemorySize) throws MaximumAllocationError {
        if (maximumMemorySize <= MAX_MEMORY_SIZE && maximumMemorySize >= MIN_MEMORY_SZIE)
            this.maximumMemorySize = maximumMemorySize;
        else
            throw new MaximumAllocationError();
    }

    public int getMemorySize() {
        return this.maximumMemorySize;
    }
    /**
     * deAllocation
     * @param pid is id of process
     * @param address is pointer to allocated memory address
     */
    public void deAllocation(int pid, long address) {
        lock.lock();

        try {

        }finally {
            lock.unlock();
        }
    }

    /**
     * allocation: for allocating memory
     * @param pid it id of process
     * @param size size of address process wants to deallocate
     * @return pointer to allocated memory
     */
    public long allocation(int pid, int size) {
        lock.lock();

        try {

        }finally {
            lock.unlock();
        }

        return 0;
    }
}
