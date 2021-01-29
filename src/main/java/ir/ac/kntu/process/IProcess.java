package ir.ac.kntu.process;

public interface IProcess extends Runnable {
    int START_SLEEP_TIME = 3;
    int END_SLEEP_TIME = 7;
    int MAX_THREAD_WORKER = 1;
    int MAX_REQUEST_SIZE = 1024;
    void deAllocation(long address);
    long allocation(int size);
}
