package ir.ac.kntu.os;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import ir.ac.kntu.process.Process;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;


public class MemoryReporter implements Runnable {
    class MemoryStruct {
        public int pid;
        public int occupiedSpaces;
        public ArrayList<Long> addresses;
        public LocalTime executionTime;
        public LocalDateTime startTime;
        public LocalDateTime endTime;
        public boolean isProcessFinished;
    }

    static private final File tempFile = new File("./log");
    static private final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-mm-dd HH-MM-ss");
    static private final Long SLEEPING_TIME = 5000L;
    private int counter;

    public MemoryReporter() {
        this.counter = 0;
        this.makeFolder();
    }

    private void makeFolder() { // check if log folder is created or not
        try {
            if (!tempFile.exists()) {
                tempFile.mkdir();
            } else {
                deleteTempFolder(tempFile);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void deleteTempFolder(File mainFolder) throws NullPointerException {
        if (mainFolder.isDirectory()) {
            for (File entry : mainFolder.listFiles()) {
                deleteTempFolder(entry);
            }
        } else {
            mainFolder.deleteOnExit();
        }
    }

    @Override
    public void run() {
        while (true) {
            this.runUtil();

            if (OsMemoryManager.getInstance().isExecutionOver())
                break;
        }
        this.runUtil();
    }

    private void runUtil() {
        try {
            Thread.sleep(SLEEPING_TIME);
        } catch (InterruptedException ex) {}

        ArrayList<MemoryStruct> memoryStructs;
        // getting lock for list of process
        Locker.getLockerInstance().readLockList();
        int internalFragment = 0;
        try {
            internalFragment = OsMemoryManager.getInstance().calculateTheInternalFragment();
            memoryStructs = this.reportMemory();
        } finally {
            Locker.getLockerInstance().readUnlockList();
        }

        int totalOccupies = OsMemoryManager.getInstance().getOccupiedSpaces();
        this.writeFile(memoryStructs, internalFragment, totalOccupies);
    }

    private ArrayList<MemoryStruct> reportMemory() {
        ArrayList<MemoryStruct> memoryStructs = new ArrayList<>();
        try {
            for (Process process : OsMemoryManager.getInstance().getProcesses()) {
                MemoryStruct memoryStruct = new MemoryStruct();
                memoryStruct.pid = process.getPid();
                memoryStruct.executionTime = this.findDifference(process.getStartTime(), LocalDateTime.now());
                memoryStruct.startTime = process.getStartTime();
                memoryStruct.occupiedSpaces = OsMemoryManager
                        .getInstance()
                        .getOccupiesOfSpecifiedProcess(process.getPid());
                memoryStruct.addresses = process.getAddresses();
                memoryStruct.endTime = null;

                boolean finish = process.isProcessOver();
                if (finish) {
                    memoryStruct.endTime = process.getEndTime();
                }
                memoryStruct.isProcessFinished = finish;
                memoryStructs.add(memoryStruct);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return memoryStructs;
    }

    private LocalTime findDifference(LocalDateTime start, LocalDateTime end) {
        int seconds = (int) ChronoUnit.SECONDS.between(start, end);
        int minutes = (int) ChronoUnit.MINUTES.between(start, end);
        if (seconds >= 60) {
            seconds %= 60;
            minutes += 1;
        }
        int hours = (int) ChronoUnit.HOURS.between(start, end);
        int milliSeconds = (int) ChronoUnit.MILLIS.between(start, end) % 1000;
        return LocalTime.of(hours, minutes, seconds, milliSeconds);
    }

    private void writeFile(ArrayList<MemoryStruct> memoryStructs, int internalFragment, int totalOccupies) {
        try {
            File file = new File(Paths.get(tempFile.getPath(), LocalDateTime.now().toString()).toString() + ".json");

            JSONObject memoryJsonObject = new JSONObject();
            JSONArray jsonArray = new JSONArray();
            for (MemoryStruct memoryStruct : memoryStructs) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Process Id", memoryStruct.pid);
                jsonObject.put("Occupied Spaces", memoryStruct.occupiedSpaces);
                jsonObject.put("Execution Time", memoryStruct.executionTime.toString());
                jsonObject.put("Start Time", memoryStruct.startTime.toString());
                if (!memoryStruct.isProcessFinished) {
                    jsonObject.put("End Time", null);
                } else {
                    jsonObject.put("End Time", memoryStruct.endTime.toString());
                }
                jsonObject.put("Is Process Finished", memoryStruct.isProcessFinished);
                jsonObject.put("Addresses", memoryStruct.addresses);
                jsonArray.add(jsonObject);
            }
            memoryJsonObject.put("Processes", jsonArray);
            memoryJsonObject.put("Internal Fragment",
                    String.valueOf(internalFragment) + "KB");
            memoryJsonObject.put("total space used",
                    String.valueOf(totalOccupies) + "KB");
            memoryJsonObject.put("total free spaces",
                    String.valueOf(OsMemoryManager.getInstance().getMemorySize() - totalOccupies) + "KB");

            try (FileWriter fileWriter = new FileWriter(file)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                JsonParser jp = new JsonParser();
                JsonElement je = jp.parse(memoryJsonObject.toJSONString());
                String prettyJsonString = gson.toJson(je);
                System.out.println("Starting Logging The Memory.");
                System.out.println(prettyJsonString);
                System.out.println("*********************************************************** " + counter);
                this.counter++;

                fileWriter.write(prettyJsonString);
                fileWriter.flush();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
