package ir.ac.kntu.process;

import java.util.ArrayList;

/**
 * ProcessRandomGenerator Generates Random Sizes And Sleep Times For each Process
 */
public class ProcessRandomGenerator {

    public static int randomRequestCount(int start, int last) {
        return (int) ProcessRandomGenerator.randomRange(start, last);
    }

    /**
     * Make Random Sizes For Each Processes
     * Until 32KB Is Prior And Each Time For Shuffling The Data It Makes Random Sizes
     * @return Size Of Allocating Sizes
     */
    public static int makeProcessWeightedProbAllocatingRequest() {
        ArrayList<Integer> weightedProp = new ArrayList<>();
        weightedProp.addAll(makeRandomForSpecificRange(10, 0, 32));
        weightedProp.addAll(makeRandomForSpecificRange(6, 32, 64));
        weightedProp.addAll(makeRandomForSpecificRange(5, 64, 128));
        weightedProp.addAll(makeRandomForSpecificRange(5, 128, 256));
        weightedProp.addAll(makeRandomForSpecificRange(5, 256, 512));
        weightedProp.addAll(makeRandomForSpecificRange(2, 256, 1024));
        try {
            return weightedProp.get((int) randomRange(0, weightedProp.size()));
        } catch (Exception ex) {
            return weightedProp.get(0);
        }
    }

    private static ArrayList<Integer> makeRandomForSpecificRange(int range, int start, int end) {
        ArrayList<Integer> rands = new ArrayList<>();
        for (int i = 0; i < range; i++) {
            int rand = (int) randomRange(start, end);
            if (rands.contains(rand)) {
                i -= 1;
                continue;
            }
            rands.add(rand);
        }
        return rands;
    }

    public static long randomSleepTime(int start, int last) {
        double randNum = ProcessRandomGenerator.randomRange(start, last);
        return (long) (randNum * 1000);
    }

    public static double randomRange(int start, int last) {
        return (Math.random() * (double) (last - start + 1)) + start;
    }

    public static Jobs randomJob() {
        int op = (int) (Math.random() * 2);
        for (Jobs jobs : Jobs.values()) {
            if (jobs.ordinal() == op)
                return jobs;
        }
        return Jobs.DEALLOCATE;
    }
}
