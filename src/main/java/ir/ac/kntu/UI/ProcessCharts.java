package ir.ac.kntu.UI;

import ir.ac.kntu.os.OsMemoryManager;
import ir.ac.kntu.process.Process;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.VBox;
import javafx.application.Platform;
import javafx.scene.text.Text;


import java.time.LocalTime;
import java.util.ArrayList;

public class ProcessCharts implements Runnable {
    private VBox mainLayout;
    private LineChart lineChart;
    private NumberAxis processAxis;
    private NumberAxis memoryAllocationAxis;
    private ArrayList<Integer> yLabels;

    public ProcessCharts(VBox vBox, double width) {
        this.mainLayout = vBox;
        yLabels = new ArrayList<>();
        processAxis = new NumberAxis();
        processAxis.setLabel("PROCESS PID");
        memoryAllocationAxis = new NumberAxis();
        memoryAllocationAxis.setLabel("ALLOCATED SIZE");
        lineChart = new LineChart(memoryAllocationAxis, processAxis);
        mainLayout.getChildren().add(lineChart);
        lineChart.setTitle("SUM OF ALLOCATING SIZES TO EACH PROCESS");
        lineChart.setMaxWidth(width - 80);
        this.makeYAxis();
    }

    private void makeYAxis() {
        for(Process process: OsMemoryManager.getInstance().getProcesses())
            yLabels.add(process.getPid());
    }

    private XYChart.Data makeXAxis(int pid) {
        int oc = OsMemoryManager.getInstance().getOccupiesOfSpecifiedProcess(pid);
        return new XYChart.Data(oc, pid);
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
        if (lineChart.getData().size() > 0) {
            lineChart.getData().remove(0);
        }
        lineChart.getData().add(series);
    }

    @Override
    public void run() {
        while(true) {
            if (OsMemoryManager.getInstance().isExecutionOver())
                break;

            try {
                Thread.sleep(3000);
            }catch (InterruptedException ex) {}

            Platform.runLater(() -> {
                this.drawChart();
            });

            try {
                Thread.sleep(1000);
            }catch (InterruptedException ex) {}
        }
    }
}
