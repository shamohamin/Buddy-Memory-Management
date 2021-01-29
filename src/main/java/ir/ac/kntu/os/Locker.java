package ir.ac.kntu.os;

import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Locker {
    private ReentrantReadWriteLock occupyListLlLock;
    private ReentrantReadWriteLock treeLock;
    private ReentrantReadWriteLock finishLock;
    private static Locker instance;

    private Locker() {
        this.occupyListLlLock = new ReentrantReadWriteLock();
        this.treeLock = new ReentrantReadWriteLock();
        this.finishLock = new ReentrantReadWriteLock();
    }

    public static Locker getLockerInstance() {
        if (Locker.instance == null) {
            Locker.instance = new Locker();
        }
        return Locker.instance;
    }

    public void readFinishLock() {
        this.finishLock.readLock().lock();
    }

    public void readFinishUnlock() {
        this.finishLock.readLock().unlock();
    }

    public void writeFinishLock() {
        this.finishLock.writeLock().lock();
    }

    public void writeFinishUnlock() {
        this.finishLock.writeLock().unlock();
    }

    public void readUnlockList() {
        this.occupyListLlLock.readLock().unlock();
    }

    public void readLockList() {
        this.occupyListLlLock.readLock().lock();
    }

    public void writeLockList() {
        this.occupyListLlLock.writeLock().lock();
    }

    public void writeUnlockList() {
        this.occupyListLlLock.writeLock().unlock();
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
