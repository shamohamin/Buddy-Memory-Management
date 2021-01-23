package ir.ac.kntu.os;

import ir.ac.kntu.errors.ExceedMemorySizeError;
import ir.ac.kntu.errors.MaximumAllocationError;
import ir.ac.kntu.errors.NoneDellocatingBlockError;

import java.util.LinkedList;
import java.util.List;

/**
 * OsMemoryManager this class implements the buddy algorithm for Memory Allocation
 */
public class OsMemoryManager implements IMemoryManager {
    private Locker locker;
    private int maximumMemorySize;
    private static OsMemoryManager instance;
    private List<Block> freeSpaces;
    private List<Block> occupiedSpaces;
    private Tree tree;


    private OsMemoryManager(int maximumMemorySize) {
        this.maximumMemorySize = maximumMemorySize;
        freeSpaces = new LinkedList<>();
        occupiedSpaces = new LinkedList<>();
        tree = new Tree(maximumMemorySize);
        locker = Locker.getLockerInstance();
    }

    public static OsMemoryManager getInstance() {
        if (instance == null){
            instance = new OsMemoryManager(1024);
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
    public void deAllocation(int pid, long address) throws NoneDellocatingBlockError {
        locker.writeLockList();

        try {
            List<Block> order = tree.postOrder();
            Block freedBlock = null;
            for(Block block: order) {
                if(block.getPidOfProcess() == pid) {
                    freedBlock = block;
                    break;
                }
            }

            if (freedBlock != null) {
                freedBlock.removeChildren(address);
                if (freedBlock.getOccupySize() == 0) {
                    freedBlock.setPidOfProcess(0);
                    freedBlock.setFree(true);
                }
                System.out.println("deallocation of process: " + pid);
                System.out.print(freedBlock.getOccupiedChildrenBlocks());
                System.out.println("*********************************************");
            }else {
                throw new NoneDellocatingBlockError();
            }


//            System.out.println("After reconstruction of Tree");
//            System.out.println(tree.postOrder());


        }finally {
            locker.readUnlockList();
        }
    }

    /**
     * allocation: for allocating memory
     * @param pid it's id of process
     * @param size size of address process wants to deallocate
     * @return pointer to allocated memory
     */
    public long allocation(int pid, int size) {
        locker.writeLockList();
        try{
            for (int i = 0; i < this.occupiedSpaces.size(); i++){
                if (this.occupiedSpaces.get(i).getPidOfProcess() == pid){
                    if (this.occupiedSpaces.get(i).getSize() - this.occupiedSpaces.get(i).getOccupySize() >= size){
                        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                        long out = this.occupiedSpaces.get(i).addChildren(size);
                        if (out == -1)
                            break;
                        System.out.println(this.occupiedSpaces.get(i).getOccupiedChildrenBlocks());
                        System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
                        return out;
                    }
                }
            }
        }finally {
            locker.writeUnlockList();
        }

        locker.writeLockTree();
        try {
            Block block = this.allocation(this.chooseCorrectSize(size));
            if (block == null) {
                return -1;
            }else {
                block.setFree(false);
                block.setPidOfProcess(pid);
                occupiedSpaces.add(block);
                System.out.println(block);
                System.out.println("Allocating " + size + "To process" + pid);
//                System.out.println(tree.postOrder());
                System.out.println(block.getOccupiedChildrenBlocks());
                System.out.println("*********************************************");

                return block.addChildren(size);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return -1;
        } finally {
            locker.writeUnlockTree();
        }
    }


    private int chooseCorrectSize(int size) throws ExceedMemorySizeError {
        if (size > MAX_BLOCK_SIZE)
            throw new ExceedMemorySizeError();
        else if (size > 512)
            return 1024;
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

    private Block allocation(int size) {
        Block block = tree.findBlock(size);
        return block;
    }


    public Tree getTree() { return tree; }

}
