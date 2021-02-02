package ir.ac.kntu.UI;

import ir.ac.kntu.os.Block;
import ir.ac.kntu.os.Locker;
import ir.ac.kntu.os.OsMemoryManager;
import ir.ac.kntu.os.Tree;

import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;


public class TreeDrawer implements Runnable {
    private ScrollPane treePane;
    private Tree tree;

    public TreeDrawer(ScrollPane treePane, Tree tree) {
        this.treePane = treePane;
        this.tree = tree;
    }

    private void drawTree() {
        // getting the lock of the tree for reading the tree
        Locker.getLockerInstance().readLockTree();
        try {
            Group group = new Group();
            group.minHeight(this.treePane.getHeight());
            group.minWidth(this.treePane.getWidth());
            this.drawTreeUtil(tree.getRoot(), 20, treePane.getHeight() / 2,
                    400, 400, group, 1);

            this.treePane.setContent(group);
        } finally {
            Locker.getLockerInstance().readUnlockTree();
        }
    }

    private void drawTreeUtil(Block root, double posX, double posY,
                              double offsetX, double offsetY, Group group, int level) {

        if (root == null)
            return;

        double avgY = (posY + posY + offsetY) / 2;

        if (root.getLeftChild() != null) {
            Line line1 = new Line(posX, posY, posX, avgY);
            Line line2 = new Line(posX, avgY, posX - offsetX, avgY);
            Line line3 = new Line(posX - offsetX, avgY, posX - offsetX, posY + offsetY);
            line1.setStrokeWidth(10);
            line2.setStrokeWidth(10);
            line3.setStrokeWidth(10);
            line1.getStyleClass().add("line-left");
            line2.getStyleClass().add("line-left");
            line3.getStyleClass().add("line-left");
            group.getChildren().addAll(line1, line2, line3);

            drawTreeUtil(root.getLeftChild(), posX - offsetX, posY + offsetY,
                    offsetX / 2, offsetY / 2, group, level + 1);
        }

        if (root.getRightChild() != null) {
            Line line1 = new Line(posX, posY, posX, avgY);
            Line line2 = new Line(posX, avgY, posX + offsetX, avgY);
            Line line3 = new Line(posX + offsetX, avgY, posX + offsetX, posY + offsetY);
            line1.setStrokeWidth(10);
            line2.setStrokeWidth(10);
            line3.setStrokeWidth(10);
            line1.getStyleClass().add("line");
            line2.getStyleClass().add("line");
            line3.getStyleClass().add("line");
            group.getChildren().addAll(line1, line2, line3);

            drawTreeUtil(root.getRightChild(), posX + offsetX, posY + offsetY,
                    offsetX / 2, offsetY / 2, group, level + 1);
        }

        // draw Circle
        Circle circle = new Circle();
        circle.setCenterX(posX);
        circle.setCenterY(posY);
        circle.setRadius(50 / level);

        Text text = new Text(String.valueOf(root.getSize()) + "KB\n" + "PID: " + root.getPidOfProcess());
        text.setX(posX - ((1. / level) * 40));
        text.setY(posY + 5);
        text.setFont(Font.font("Verdana", FontWeight.EXTRA_BOLD, (50. / level / 2.)));
        text.getStyleClass().add("text");
        circle.getStyleClass().add("circle");

        if (root.isFree()) {
            circle.setFill(Color.rgb(11, 208, 81, 1));
            circle.getStyleClass().add("circle-green");
        } else {
            circle.setFill(Color.rgb(250, 8, 0, 0.8));
            circle.getStyleClass().add("circle-red");
        }
        group.getChildren().addAll(circle, text);
    }

    @Override
    public void run() {
        while (true) {

            try {
                Thread.sleep(3000);
            } catch (Exception ex) {
            }

            Platform.runLater(() -> {
                this.drawTree();
            });
            // delay until execution of the Platform is over

            //  if execution of process are over
            if (OsMemoryManager.getInstance().isExecutionOver())
                break;
        }

        OsMemoryManager.getInstance().mergingFreedBlocks();
        try {
            Thread.sleep(1000l);
        }catch (InterruptedException ex) {}
        OsMemoryManager.getInstance().mergingFreedBlocks();
        // for last one
        Platform.runLater(() -> {
            this.drawTree();
        });
    }
}
