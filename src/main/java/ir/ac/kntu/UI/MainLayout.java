package ir.ac.kntu.UI;

import ir.ac.kntu.os.OsMemoryManager;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;


public class MainLayout implements UiConfig {
    private Pane root;
    private ScrollPane treeScene;
    private ChoiceBox choiceBox;
    private ObservableList list;
    private static Integer sleepTime;

    public MainLayout(Pane root) {
        this.root = root;
        initUi();
    }

    private void initUi() {
        // mainLayout
        HBox hBox = new HBox();
        hBox.setMinHeight(MAX_HEIGHT);
        hBox.setMinWidth(MAX_WIDTH);

        // treePane
        HBox hBox1 = new HBox();
        // charts
        HBox hBox2 = new HBox();
        VBox vBox = new VBox();
//        VBox vBox1 = new VBox();

        vBox.getStyleClass().add("chart");
        hBox1.getStyleClass().add("panel");

        treeScene = new ScrollPane();
        treeScene.getStyleClass().add("tree");
        treeScene.setBackground(new Background(
                new BackgroundFill(Color.rgb(34, 41, 48, 1), CornerRadii.EMPTY, Insets.EMPTY)
        ));

        vBox.setMinWidth(MAX_WIDTH/2);
        vBox.setMaxHeight(MAX_HEIGHT);

//        vBox1.setMinWidth(MAX_WIDTH/2);
//        vBox1.setMaxHeight(MAX_HEIGHT/2);

        hBox1.setMinWidth( 55*MAX_WIDTH/100.);
        hBox1.setMinHeight(MAX_HEIGHT);

        treeScene.setPrefSize(55*MAX_WIDTH / 100., MAX_HEIGHT / 2);

        treeScene.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        treeScene.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
//        treeScene.setMaxHeight(MAX_HEIGHT / 2);
        Thread thread = new Thread(new TreeDrawer(treeScene, OsMemoryManager.getInstance().getTree()));
        thread.setDaemon(true);
        thread.start();
        Thread thread1 = new Thread(new ProcessCharts(vBox, MAX_WIDTH/2 - 10));
        thread1.setDaemon(true);
        thread1.start();

        hBox1.getChildren().add(treeScene);

        hBox2.setMinWidth(MAX_WIDTH/2);
        hBox2.setMinHeight(MAX_HEIGHT);

        hBox2.getChildren().addAll(vBox);
        hBox.setSpacing(1);

        hBox.getChildren().addAll(hBox1, hBox2);

        root.getChildren().add(hBox);
    }

}
