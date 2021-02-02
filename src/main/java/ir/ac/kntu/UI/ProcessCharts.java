package ir.ac.kntu.UI;

import ir.ac.kntu.os.Locker;
import ir.ac.kntu.os.OsMemoryManager;
import ir.ac.kntu.process.Process;
import javafx.scene.chart.*;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.scene.text.Text;


import java.time.LocalTime;
import java.util.ArrayList;

/**
 * ProcessCharts makes the Charts of process Allocation and the memory reporting chart
 */
public class ProcessCharts implements Runnable {
    private VBox mainLayout;
    private LineChart lineChart;
    private NumberAxis processAxis;
    private NumberAxis memoryAllocationAxis;
    private NumberAxis memoryUsage;
    private CategoryAxis categoryAxis;
    private LineChart<String, Number> barChart;
    private ArrayList<Integer> yLabels;
    private int mainMemorySize;
    private static int MAX_SHOWING_HISTORY = 3;

    public ProcessCharts(VBox vBox, double width) {
        this.mainLayout = vBox;
        this.mainMemorySize = OsMemoryManager.getInstance().getMemorySize();

        categoryAxis = new CategoryAxis();
        memoryUsage = new NumberAxis();
        barChart = new LineChart<>(categoryAxis, memoryUsage);
        barChart.setTitle("Memory Info");

        yLabels = new ArrayList<>();
        processAxis = new NumberAxis();
        processAxis.setLabel("PROCESS PID");
        memoryAllocationAxis = new NumberAxis();
        memoryAllocationAxis.setLabel("ALLOCATED SIZE");
        lineChart = new LineChart(processAxis, memoryAllocationAxis);
        lineChart.setTitle("SUM OF ALLOCATING SIZES TO EACH PROCESS");
        lineChart.setMaxWidth(width - 80);
        barChart.setMaxWidth(width - 80);

        mainLayout.getChildren().addAll(lineChart, barChart);
        this.makeYAxis();
    }

    private void makeYAxis() {
        for(Process process: OsMemoryManager.getInstance().getProcesses())
            yLabels.add(process.getPid());
    }

    private XYChart.Data makeXAxis(int pid) {
        int oc = OsMemoryManager.getInstance().getOccupiesOfSpecifiedProcess(pid);
        return new XYChart.Data(pid, oc);
    }

    private XYChart.Series makeData() {
        XYChart.Series series = new XYChart.Series();
        try {
            Text text = new Text(LocalTime.now().toString());
            text.setStyle("color: black");
            series.setName(text.getText());
            for (Integer label:this.yLabels)
                series.getData().add(this.makeXAxis(label));
            return  series;
        }catch (Exception ex) {
            return series;
        }
    }

    private void drawChart() {
        XYChart.Series series = this.makeData();
        if (lineChart.getData().size() > MAX_SHOWING_HISTORY) {
            lineChart.getData().remove(0);
        }
        lineChart.getData().add(series);
    }

    private void drawMemoryChart() {
        XYChart.Series series = new XYChart.Series();

        int totalOccupies = 0;
        int internalFragment = 0;
        // locking
        Locker.getLockerInstance().readLockList();
        try {
            internalFragment = OsMemoryManager.getInstance().calculateTheInternalFragment();
        }finally {
            Locker.getLockerInstance().readUnlockList();
        }
        totalOccupies = OsMemoryManager.getInstance().getOccupiedSpaces();

        series.setName(LocalTime.now().toString());
        series.getData().add(new  XYChart.Data<String, Number>("Memory Size", this.mainMemorySize));
        series.getData().add(new  XYChart.Data<String, Number>("Occupied Sizes", totalOccupies));
        series.getData().add(new  XYChart.Data<String, Number>("Internal Fragment", internalFragment));
        if (barChart.getData().size() > 2){
            barChart.getData().remove(0);
        }
        this.barChart.getData().add(series);
    }

    @Override
    public void run() {
        while(true) {
            try {
                Thread.sleep(3000);
            }catch (InterruptedException ex) {}

            Platform.runLater(() -> {
                this.drawChart();
                this.drawMemoryChart();
            });

            try {
                Thread.sleep(1000);
            }catch (InterruptedException ex) {}

            if (OsMemoryManager.getInstance().isExecutionOver())
                break;
        }
        try {
            Thread.sleep(1000);
        }catch (InterruptedException ex) {}
        Platform.runLater(() -> {
            this.drawChart();
            this.drawMemoryChart();
        });
    }
}
