package ir.ac.kntu.os;


import java.util.LinkedList;
import java.util.List;

public class Tree {
    private Block root;

    public Tree(int rootSize) {
        this.root = new Block(rootSize, 0x0, true, 0);
    }

    public Block findBlock(int size) {
        return findBlock(root, size, 0);
    }

    private Block findBlock(Block root, int size, long address) {
        if (!root.isFree())
            return null;

        if (this.dividableBlock(root) && root.getSize() >= size && size > root.getSize() / 2) {
            if (root.isFree()) {
                return root;
            }
            return null;
        }else if(size <= root.getSize() / 2) {
            if (dividableBlock(root)) {
                Block leftBlock = new Block(root.getSize() / 2, address, true, 0);
                Block rightBlock = new Block(root.getSize() / 2, address + (root.getSize() / 2), true, 0);
                leftBlock.setParent(root); rightBlock.setParent(root);
                root.setLeftChild(leftBlock);
                root.setRightChild(rightBlock);
            }
            Block leftTemp = findBlock(root.getLeftChild(), size, address);
            if (leftTemp != null) return leftTemp;
            Block rightTemp = findBlock(root.getRightChild(), size, root.getSize() / 2);
            if (rightTemp != null) return rightTemp;

            return null;
        }

        return null;
    }

    /**
     * this method is for merging the blocks which is freed tu make larger block
     */
    public void reconstructTree() {
        List<Block> blocks = this.postOrder();
        for (int i = 0; i < blocks.size(); i++){
            Block temp = blocks.get(i);
            if (temp.isFree()){
                while(temp.getParent() != null && mergeIsPossible(temp)) {
                    temp.setLeftChild(null);
                    temp.setRightChild(null);
                    temp = temp.getParent();
                }
            }
        }
    }

    private boolean mergeIsPossible(Block root) {
        if (!root.isFree())
            return false;

        if(root == null)
            return true;

        boolean left = true;
        boolean right = true;
        if (root.getLeftChild() != null)
            left = mergeIsPossible(root.getLeftChild());

        if (root.getLeftChild() != null)
            right = mergeIsPossible(root.getRightChild());

        return left && right;
    }

    /**
     * use this method for seeing blocks children currently aren't currently used
     * @param root is parent block
     * @return has children block used
     */
    private boolean dividableBlock(Block root) {
        if (root.getLeftChild() == null && root.getRightChild() == null)
            return true;
        return false;
    }

    public List<Block> postOrder() {
        List<Block> order = new LinkedList<>();
        postOrder(this.root, order);
        return order;
    }

    private void postOrder(Block root, List<Block> order) {
        if (root == null)
            return;

        if (root.getLeftChild() != null)
            postOrder(root.getLeftChild(), order);
        if (root.getRightChild() != null)
            postOrder(root.getRightChild(), order);

        order.add(root);
    }

    public Block getRoot() {
        return root;
    }

}
