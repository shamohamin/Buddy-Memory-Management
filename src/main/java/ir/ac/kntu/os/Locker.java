package ir.ac.kntu.os;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Locker {
    private ReentrantReadWriteLock occupyListLlLock;
    private ReentrantReadWriteLock treeLock;
    private static Locker instance;

    private Locker() {
        this.occupyListLlLock = new ReentrantReadWriteLock();
        this.treeLock = new ReentrantReadWriteLock();
    }

    public static Locker getLockerInstance() {
        if (Locker.instance == null)
            Locker.instance = new Locker();

        return Locker.instance;
    }

    public void readUnlockList() {
        this.occupyListLlLock.readLock().lock();
    }

    public void readLockList() {
        this.occupyListLlLock.readLock().unlock();
    }

    public void writeLockList() {
        this.treeLock.writeLock().lock();
    }

    public void writeUnlockList() {
        this.treeLock.writeLock().unlock();
    }

    public void readLockTree() {
        this.treeLock.readLock().lock();
    }

    public void readUnlockTree() {
        this.treeLock.readLock().unlock();
    }

    public void writeLockTree() {
        this.treeLock.writeLock().lock();
    }

    public void writeUnlockTree() {
        this.treeLock.writeLock().unlock();
    }

}
