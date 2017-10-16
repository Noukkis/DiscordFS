/*
 * The MIT License
 *
 * Copyright 2017 Noukkis.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package discordfs.wrk;

import java.util.ArrayList;
import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 *
 * @author Jordan Vesy
 */
public class Updater {

    private VBox root;
    private ArrayList<Task> queue;
    private Stage stage;

    public Updater() {
        queue = new ArrayList<>();
        root = new VBox();
        ScrollPane sp = new ScrollPane(root);
        sp.setFitToWidth(true);
        sp.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        sp.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        stage = new Stage();
        Scene scene = new Scene(sp);
        stage.setScene(scene);
        stage.setTitle("Queue");
        stage.setHeight(400);
        stage.setWidth(600);
    }

    public void show() {
        stage.show();
    }

    public void addTask(String name, Task task) {
        ProgressBar pb = new ProgressBar();
        Label lblTitle = new Label(name);
        pb.progressProperty().bind(task.progressProperty());
        VBox vb = new VBox(5, lblTitle, pb, new Separator(Orientation.HORIZONTAL));
        vb.setAlignment(Pos.CENTER_LEFT);
        root.getChildren().add(vb);
        lblTitle.setPrefWidth(600);
        pb.setPrefWidth(600);
        EventHandler before = task.getOnSucceeded();
        task.setOnSucceeded((event) -> {
            root.getChildren().remove(vb);
            queue.remove(task);
            if (queue.size() > 0) {
                new Thread(queue.get(0)).start();
            }
            if (before != null) {
                before.handle(event);
            }
        });
        queue.add(task);
        if (queue.size() == 1) {
            new Thread(task).start();
        }
    }
}
