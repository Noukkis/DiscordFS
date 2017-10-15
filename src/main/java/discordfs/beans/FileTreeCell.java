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
package discordfs.beans;

import discordfs.Ctrl;
import java.io.File;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 *
 * @author Jordan Vesy
 */
public class FileTreeCell extends TreeCell<FileView> {
    
    private static final String IMG_FOLDER = "/images/folder.png";
    private static final String IMG_FILE = "/images/file.png";

    private Ctrl ctrl;
    

    public FileTreeCell(Ctrl ctrl) {
        this.ctrl = ctrl;
    }

    @Override
    protected void updateItem(FileView item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item.toString());
            setGraphic(new ImageView(item.isDirectory() ? IMG_FOLDER : IMG_FILE));
        }

        setOnDragOver((event) -> {
            if (!empty && item != null && item.isDirectory()) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
                setStyle("-fx-effect: innershadow( gaussian, DodgerBlue, 2, 1, 0, 0 );");
            }
        });

        setOnDragExited((event) -> {
            setStyle("");
        });

        setOnDragDropped((event) -> {
            if (!empty && item != null && item.isDirectory()) {
                for (File file : event.getDragboard().getFiles()) {
                    FileView fw = ctrl.getWrk().upload(file, (DirectoryView) item);
                    TreeItem<FileView> newItem = ctrl.createTreeItem(fw);
                    getTreeItem().getChildren().add(newItem);
                }
            }
        });
    }
}
