package ir.ac.kntu;

import ir.ac.kntu.UI.MainLayout;
import ir.ac.kntu.UI.UiConfig;
import ir.ac.kntu.os.OsMemoryManager;
import ir.ac.kntu.process.IProcessConfig;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;


public class Main extends Application implements UiConfig, IProcessConfig {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Pane root = new Pane();
        OsMemoryManager.getInstance();
        try {
//            System.out.println(OsMemoryManager.getInstance().allocation(1, 1023));
//            System.out.println(OsMemoryManager.getInstance().allocation(1, 700));
//            System.out.println(OsMemoryManager.getInstance().allocation(1, 300));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            root.getStylesheets().add(
                    this.getClass()
                            .getResource("/style.css")
                            .toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        root.getStyleClass().add("root");
        Scene scene = new Scene(root, MAX_WIDTH, MAX_HEIGHT);

        new MainLayout(root);

        stage.setTitle("Processes");
        stage.setScene(scene);
        stage.show();
    }


//    private void initApp() {
//        List<Future> process = new ArrayList<>();
//        try {
//            System.out.println("hellooo");
//            for(int i = 1; i <= MAX_WORKER_PROCESS; i++) {
//                process.add(executorService.submit(
//                        new Process(i, ProcessRandomGenerator.randomRequestCount(MIN_JOB_PROCESS, 12))
//                ));
//            }
//            executorService.submit(() -> {
//                // for merging the blocks if its possible after every 2 seconds
//                while(true){
//                    // if execution is over break finish the process
//                    if (OsMemoryManager.getInstance().isExecutionOver())
//                        break;
//                    try {
//                        Thread.sleep(2000);
//                    }catch (Exception ex) {
//                        ex.printStackTrace();
//                    }
//                    OsMemoryManager.getInstance().mergingFreedBlocks();
//                }
//            });
//        }catch (Exception ex) {
//            System.err.println("ERROR IN INITIALIZING THE PROCESS");
//        } finally {
//            executorService.shutdown();
//        }
//    }

}
