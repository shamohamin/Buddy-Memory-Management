package ir.ac.kntu;

import ir.ac.kntu.errors.MaximumAllocationError;
import ir.ac.kntu.os.OsMemoryManager;

public class Main {
    public static void main(String[] args) {
        OsMemoryManager osMemoryManager = OsMemoryManager.getInstance();
        try {
            osMemoryManager.setMemorySize(4086);
        }catch (MaximumAllocationError ex) {
            ex.printStackTrace();
        }
        System.out.print(osMemoryManager.getMemorySize());
    }
}
