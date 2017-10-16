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

import discordfs.helpers.Statics;
import discordfs.wrk.FilesWrk;
import java.util.Objects;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;

/**
 *
 * @author Jordan Vesy
 */
public class FileItem extends TreeItem<String> {

    private final String name;
    private boolean root;
    private final String id;

    public FileItem(FilesWrk wrk, String name, String id, boolean root) {
        super(name);
        this.name = name;
        this.root = root;
        this.id = id;
        if (isDirectory()) {
            expandedProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue) {
                    new Thread(() -> {
                        for (TreeItem<String> c : getChildren()) {
                            FileItem child = (FileItem) c;
                            child.setExpanded(false);
                            if (child.isDirectory()) {
                                DirectoryItem dir = (DirectoryItem) child;
                                wrk.setDirectoryChildren(dir);
                                child.setGraphic(new ImageView(Statics.IMG_FOLDER));
                            }
                            String temp = child.getValue();
                            child.setValue(null);
                            child.setValue(temp);
                        }
                    }).start();
                }
            });
        }
        setGraphic(new ImageView(isRoot() ? Statics.IMG_FOLDER
                : isDirectory() ? Statics.IMG_LOADING_FOLDER
                        : Statics.IMG_FILE));
    }

    public FileItem(FilesWrk wrk, String name, String id) {
        this(wrk, name, id, false);
    }

    public int getSize() {
        return 0;
    }

    public DirectoryItem getParentDir() {
        return (DirectoryItem) getParent();
    }

    public boolean isDirectory() {
        return false;
    }

    public String getName() {
        return (root) ? "" : name;
    }

    public String getId() {
        return id;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    public boolean isRoot() {
        return root;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileItem other = (FileItem) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
