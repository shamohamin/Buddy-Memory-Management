package ir.ac.kntu.process;


import ir.ac.kntu.errors.ExceedMemorySizeError;
import ir.ac.kntu.errors.NoneAllocatingBlock;
import ir.ac.kntu.errors.NoneDellocatingBlockError;
import ir.ac.kntu.os.OsMemoryManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

public class Process implements IProcess {
    private int pid;
    private ExecutorService executors;
    private int requestCount;
    private List<Long> holdingRequestedAddress;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private boolean finished;
    private ReentrantLock listAndFinishLock;

    public ArrayList<Long> getAddresses() {
        this.listAndFinishLock.lock();
        try {
            return new ArrayList<>(holdingRequestedAddress);
        } finally {
            this.listAndFinishLock.unlock();
        }
    }

    public int getPid() {
        return pid;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Process(int pid, int requestCount) {
        this.pid = pid;
        this.executors = Executors.newFixedThreadPool(MAX_THREAD_WORKER);
        this.requestCount = requestCount;
        this.holdingRequestedAddress = new ArrayList<>();
        this.startTime = LocalDateTime.now();
        this.finished = false;
        this.listAndFinishLock = new ReentrantLock();
    }

    public void deAllocation(long address) {
        try {
//            System.out.printf("deAllocating address is %d\n", address);
            OsMemoryManager.getInstance().deAllocation(this.pid, address);
        } catch (NoneDellocatingBlockError ex) {
            System.err.println("THIS ADDRESS WAS NOT ALLOCATED TO BE FREED");
        }
    }

    public long allocation(int size) {
//        int size = (int) ProcessRandomGenerator.randomRange(1, MAX_REQUEST_SIZE);
        try {
            long address = OsMemoryManager.getInstance().allocation(this.pid, size);
//            System.out.printf("SIZE IS: %d  return address is: %d \n", size, address);
            return address;
        } catch (NoneAllocatingBlock | ExceedMemorySizeError ex) {
            System.err.printf(
                    "MEMORY DOESN'T HAVE ENOUGH SPACE FOR ALLOCATING THIS SIZE: %d, TO PROCESS %d\n", size, pid);
            return -1;
        }
    }

    @Override
    public void run() {
        ArrayList<Future> t = new ArrayList<>();
        while (this.requestCount >= 0) {
            // sleeping for random milliseconds
            try {
                Thread.sleep(ProcessRandomGenerator.randomSleepTime(START_SLEEP_TIME, END_SLEEP_TIME));
            } catch (InterruptedException ex) {
            }

            try {
                Jobs job;
                this.listAndFinishLock.lock();
                try {
                    if (this.holdingRequestedAddress.size() == 0)
                        job = Jobs.ALLOCATING;
                    else
                        job = ProcessRandomGenerator.randomJob();
                }finally {
                    this.listAndFinishLock.unlock();
                }

                switch (job) {
                    case ALLOCATING:
                        t.add(this.executors.submit(() -> {
                            int size = ProcessRandomGenerator.makeProcessWeightedProbAllocatingRequest();
                            long address = this.allocation(size);
                            if (address != -1) {
                                this.listAndFinishLock.lock();
                                try {
                                    this.holdingRequestedAddress.add(address);
                                } finally {
                                    this.listAndFinishLock.unlock();
                                }
                            }
                        }));
                        break;
                    default:
                        t.add(this.executors.submit(() -> {
                            int s;
                            this.listAndFinishLock.lock();
                            try {
                                s = this.holdingRequestedAddress.size();
                            } finally {
                                this.listAndFinishLock.unlock();
                            }

                            if (s != 0) {
                                int pos = (int) ProcessRandomGenerator.randomRange(0, s);
                                Long l = 0l;
                                this.listAndFinishLock.lock();
                                try {
                                    l = this.holdingRequestedAddress.get(pos);
                                    this.holdingRequestedAddress.remove(pos);
                                } finally {
                                    this.listAndFinishLock.unlock();
                                }
                                this.deAllocation(l);
                            }
                        }));
                }
            } catch (Exception ex) {
                System.err.printf("Inside process: %d something went wrong.", this.pid);
                ex.printStackTrace();
            }

            this.requestCount--;
        }
        this.executors.shutdown();
        // waiting until all threads are shutdown
        for (Future future : t) {
//            if (!future.isDone()) {
                try {
                    future.get();
                } catch (Exception ex) {}
//            }
        }
        // deAllocating entire process Address
        this.listAndFinishLock.lock();
        try {
            for (Long address : this.holdingRequestedAddress) {
                this.deAllocation(address);
            }
            this.holdingRequestedAddress = new ArrayList<>();
        } finally {
            this.listAndFinishLock.unlock();
        }

        // tell the other Process iam over
        OsMemoryManager.getInstance().incrementTheFinishVariable();

        try {
            Thread.sleep(1000l);
        }catch (InterruptedException ex){}

        this.listAndFinishLock.lock();
        try {
            this.finished = true;
            this.endTime = LocalDateTime.now();
        } finally {
            this.listAndFinishLock.unlock();
        }
    }

    public boolean isProcessOver() {
        this.listAndFinishLock.lock();
        try {
            if (this.finished)
                return true;
            return false;
        } finally {
            this.listAndFinishLock.unlock();
        }
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
