package ir.ac.kntu;

import ir.ac.kntu.UI.MainLayout;
import ir.ac.kntu.UI.UiConfig;
import ir.ac.kntu.os.OsMemoryManager;
import ir.ac.kntu.process.IProcessConfig;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @author MoahammadAmin Shafiee
 */
public class Main extends Application implements UiConfig, IProcessConfig {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        Pane root = new Pane();
        this.initApp();

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

    private void initApp() {
        OsMemoryManager.getInstance();
    }

}
