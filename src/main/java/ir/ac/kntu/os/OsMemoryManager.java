package ir.ac.kntu.os;

import ir.ac.kntu.errors.ExceedMemorySizeError;
import ir.ac.kntu.errors.MaximumAllocationError;
import ir.ac.kntu.errors.NoneAllocatingBlock;
import ir.ac.kntu.errors.NoneDellocatingBlockError;
import ir.ac.kntu.process.IProcessConfig;
import ir.ac.kntu.process.Process;
import ir.ac.kntu.process.ProcessRandomGenerator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * OsMemoryManager This Class Implements The Buddy Memory Management Algorithm For Memory Allocation
 */
public class OsMemoryManager implements IMemoryManager, IProcessConfig {
    private Locker locker;
    private int maximumMemorySize;
    private static OsMemoryManager instance;
    private List<Block> freeSpaces;
    private List<Block> occupiedSpaces;
    private List<Process> processes;
    private int isAllocatingOver;
    private ExecutorService executorService;
    private int internalFragmentation;
    private int totalMemoryUsed;
    private Tree tree;

    private OsMemoryManager(int maximumMemorySize) {
        this.isAllocatingOver = 0;
        this.internalFragmentation = 0;
        this.maximumMemorySize = maximumMemorySize;
        freeSpaces = new LinkedList<>();
        occupiedSpaces = new LinkedList<>();
        tree = new Tree(maximumMemorySize);
        locker = Locker.getLockerInstance();
        processes = new ArrayList<>();
        this.executorService = Executors.newCachedThreadPool();
        this.makeProcesses();
        this.initApp();
    }

    public static OsMemoryManager getInstance() {
        if (instance == null) {
            instance = new OsMemoryManager(2048);
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
     *
     * @param pid     is id of process
     * @param address is pointer to allocated memory address
     */
    public void deAllocation(int pid, long address) throws NoneDellocatingBlockError {
        locker.writeLockList();
        int i = -1;
        try {
            Block freedBlock = null;
            for (int j = 0; j < this.occupiedSpaces.size(); j++) {
                Block block = this.occupiedSpaces.get(j);
                if (block.getPidOfProcess() == pid) { // if process has block which was recently occupied by process
                    boolean found = false;
                    for (Block childBlock : block.getOccupiedChildrenBlocks()) {
                        if (childBlock.getAddress() == address) {
                            found = true;
                            freedBlock = block;
                            i = j;
                            break;
                        }
                    }
                    if (found)
                        break;
                }
            }

            if (freedBlock != null) {
                this.locker.writeLockTree();
                try {
                    freedBlock.removeChildren(address);
                    if (freedBlock.getOccupySize() == 0) {
                        freedBlock.setPidOfProcess(0);
                        freedBlock.setFree(true);
                        this.occupiedSpaces.remove(i);
                    }
                } finally {
                    this.locker.writeUnlockTree();
                }
            } else {
                throw new NoneDellocatingBlockError();
            }
        } finally {
            locker.writeUnlockList();
        }
    }

    /**
     * allocation: for allocating memory
     *
     * @param pid  it's id of process
     * @param size size of address process wants to deallocate
     * @return pointer to allocated memory
     */
    public long allocation(int pid, int size) throws NoneAllocatingBlock, ExceedMemorySizeError {
        locker.writeLockList();
        // if it has some place which we can put it
        try {
            for (int i = 0; i < this.occupiedSpaces.size(); i++) {
                if (this.occupiedSpaces.get(i).getPidOfProcess() == pid) {
                    if (this.occupiedSpaces.get(i).getSize() - this.occupiedSpaces.get(i).getOccupySize() >= size) {
                        locker.writeLockTree();
                        try {
                            long out = this.occupiedSpaces.get(i).addChildren(size);
                            if (out == -1)
                                break;
                            return out;
                        }finally {
                            locker.writeUnlockTree();
                        }
                    }
                }
            }
        } finally {
            locker.writeUnlockList();
        }

        locker.writeLockTree();
        try {
            Block block = this.allocation(this.chooseCorrectSize(size));
            if (block == null) {
                throw new NoneAllocatingBlock();
            } else {
                block.setFree(false);
                block.setPidOfProcess(pid);

                locker.writeLockList();
                try {
                    occupiedSpaces.add(block);
                    return block.addChildren(size);
                } finally {
                    locker.writeUnlockList();
                }
            }
        } finally {
            locker.writeUnlockTree();
        }
    }


    private int chooseCorrectSize(int size) throws ExceedMemorySizeError {
        if (size > MAX_BLOCK_SIZE)
            throw new ExceedMemorySizeError();
        else if (size > 512 || size == MAX_BLOCK_SIZE)
            return MAX_BLOCK_SIZE;
        else if (size > 256)
            return 512;
        else if (size > 128)
            return 256;
        else if (size > 64)
            return 128;
        else if (size > 32)
            return 64;
        else
            return 32;
    }

    public void mergingFreedBlocks() {
        this.locker.writeLockTree();
        try {
            this.tree.reconstructTree();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            this.locker.writeUnlockTree();
        }
    }

    private Block allocation(int size) {
        Block block = tree.findBlock(size);
        return block;
    }


    public void incrementTheFinishVariable() {
        this.locker.writeFinishLock();
        try {
            this.isAllocatingOver++;
        } finally {
            this.locker.writeFinishUnlock();
        }
    }

    public boolean isExecutionOver() {
        locker.readFinishLock();
        try {
            if (this.isAllocatingOver >= MAX_WORKER_PROCESS) {
                locker.writeLockList();
                locker.writeLockTree();
                try {
                    for (Block block: this.occupiedSpaces){
                        block.setFree(true);
                        block.setPidOfProcess(0);
                        block.setOccupySize(0);
                        block.setOccupiedChildrenBlocks(new ArrayList<>());
                    }
                    this.occupiedSpaces = new ArrayList<>();
                }finally {
                    locker.writeUnlockList();
                    locker.writeUnlockTree();
                }
                if (this.getOccupiedSpaces() == 0)
                    return true;
                return false;
            }
            return false;
        } finally {
            locker.readFinishUnlock();
        }
    }

    private void makeProcesses() {
        for (int i = 1; i <= MAX_WORKER_PROCESS; i++) {
            this.processes.add(
                    new Process(i, ProcessRandomGenerator.randomRequestCount(MIN_JOB_PROCESS, MAX_JOB_PROCESS))
            );
        }
    }

    private void initApp() {
        try {
            for (Process process : this.processes) {
                executorService.submit(process);
            }
            // Initializing The MemoryReporter
            executorService.submit(new MemoryReporter());
            this.initMergingThread();
        } catch (Exception ex) {
            System.err.println("ERROR IN INITIALIZING THE PROCESS");
        } finally {
            executorService.shutdown();
        }
    }

    private void initMergingThread() {
        executorService.submit(() -> {
            // for merging the blocks if its possible after every 2 seconds
            while (true) {
                try {
                    Thread.sleep(2000);
                } catch (Exception ex) {
                }

                this.mergingFreedBlocks();

                // if execution is over break finish the process
                if (this.isExecutionOver())
                    break;
            }

            // for last one
            this.mergingFreedBlocks();
        });
    }

    public int getOccupiedSpaces() {
        this.locker.readLockList();
        this.totalMemoryUsed = 0;
        try {
            for (Block block : this.occupiedSpaces)
                this.totalMemoryUsed += block.getOccupySize();
        } finally {
            this.locker.readUnlockList();
        }

        return this.totalMemoryUsed;
    }

    public int getOccupiesOfSpecifiedProcess(int pid) {
        int occupiedSizes = 0;
        for (Block block : this.occupiedSpaces) {
            if (block.getPidOfProcess() == pid) {
                occupiedSizes += block.getOccupySize();
            }
        }
        return occupiedSizes;
    }

    public int calculateTheInternalFragment() {
        this.internalFragmentation = 0;
        for (Block block : this.occupiedSpaces) {
            this.internalFragmentation += (block.getSize() - block.getOccupySize());
        }
        return this.internalFragmentation;
    }
    // first must acquire the lock
    public Tree getTree() {
        return tree;
    }

    public ArrayList<Process> getProcesses() {
        return new ArrayList<>(processes);
    }

}
