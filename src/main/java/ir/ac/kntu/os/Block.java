package ir.ac.kntu.os;

import java.util.*;

public class Block {
    private int size;
    private long address;
    private boolean isFree;
    private Block leftChild;
    private Block rightChild;
    private Block parent;
    private long pidOfProcess;
    private int occupySize;
    private List<Block> occupiedChildrenBlocks;

    public Block(int size, long address, boolean isFree, int occupySize) {
        this.size = size;
        this.address = address;
        this.isFree = isFree;
        this.occupySize = occupySize;
        this.leftChild = null;
        this.rightChild = null;
        this.parent = null;
        occupiedChildrenBlocks = new LinkedList<>();
    }

    private Block(int size, long address) {
        this.size = size;
        this.address = address;
    }

    /**
     * addChildren use first fit algorithm for placing buffer inside it
     * @param size it's size of block requested
     * @return address of allocated space if it can otherwise -1 (this request is not allowable)
     */
    public long addChildren(int size) {
        // implementing first fit algorithm
        Collections.sort(this.occupiedChildrenBlocks, (o1, o2) -> (int) (o1.getAddress() - o2.getAddress()));
        boolean flag = false;
        long addressPointer = this.address;
        for(int i = 0; i < this.occupiedChildrenBlocks.size(); i++) {
            Block b = this.occupiedChildrenBlocks.get(i);
            if (addressPointer + size <= b.getAddress()) {
                flag = true;
                break;
            }
            // it will be the end of this block b
            addressPointer = b.address + b.getSize();
        }

        if ((flag || addressPointer < this.address + this.size) && addressPointer + size < this.address + this.size) {
            this.occupySize += size;
            this.occupiedChildrenBlocks.add(new Block(size, addressPointer));
            return addressPointer;
        }else {
            return  -1;
        }
    }

    public void removeChildren(long address) {
        for (int i = 0; i < this.occupiedChildrenBlocks.size(); i++){
            if(this.occupiedChildrenBlocks.get(i).address == address) {
                this.occupySize -= this.occupiedChildrenBlocks.get(i).size;
                this.occupiedChildrenBlocks.remove(i);
            }
        }
    }

    public void setParent(Block parent) { this.parent = parent; }

    public Block getParent() { return parent; }

    public int getSize() {
        return size;
    }

    public void setFree(boolean free) { isFree = free; }

    public void setPidOfProcess(long pidOfProcess) {
        this.pidOfProcess = pidOfProcess;
    }

    public int getOccupySize() { return occupySize; }

    public long getPidOfProcess() {
        return pidOfProcess;
    }

    public long getAddress() {
        return address;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setOccupySize(int occupySize) {
        this.occupySize = occupySize;
    }

    public Block getLeftChild() { return leftChild; }

    public void setLeftChild(Block leftChild) { this.leftChild = leftChild; }

    public void setRightChild(Block rightChild) { this.rightChild = rightChild; }

    public Block getRightChild() { return rightChild; }

    public List<Block> getOccupiedChildrenBlocks() {
        return occupiedChildrenBlocks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Block block = (Block) o;
        return size == block.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, address, isFree);
    }

    @Override
    public String toString() {
        String l = leftChild == null ? "null" : String.valueOf(leftChild.getSize());
        String r = rightChild == null ? "null" : String.valueOf(rightChild.getSize());
        return "Block{" +
                "size=" + size +
                ", address=" + address +
                ", isFree=" + isFree +
                ", leftChild=" + l +
                ", rightChild=" + r +
                ", processId=" + pidOfProcess +
                '}';
    }
}
