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

import discordfs.beans.DirectoryItem;
import discordfs.beans.FileItem;
import discordfs.helpers.PropertiesManager;
import discordfs.helpers.Statics;
import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 *
 * @author Jordan Vesy
 */
public class FilesWrk {

    private final DiscordWrk discord;
    private final Updater updater;
    private final TaskMaker tm;

    public FilesWrk(DiscordWrk discord) {
        this.discord = discord;
        updater = new Updater();
        tm = new TaskMaker(this, discord);
    }

    public void createFolder(FileItem f, String name) {
        DirectoryItem parent = f.isDirectory() ? (DirectoryItem) f : f.getParentDir();
        Task<DirectoryItem> task = tm.createFolder(parent, name);
        task.setOnSucceeded((event) -> {
            try {
                task.get().setGraphic(new ImageView(Statics.IMG_FOLDER));
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(FilesWrk.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        updater.addTask("Creating \"" + name + "\" folder", task);
    }

    public void download(File dir, FileItem fi) {
        Task<Void> task = tm.download(dir, fi);
        updater.addTask("downloading \"" + fi.getName() + "\"", task);
    }

    public void delete(FileItem f) {
        if (f.isRoot()) {
            PropertiesManager.setRootMessageID(discord.treeSend("??"));
        } else {
            Task<Void> task = tm.delete(f, f.getParentDir());
            updater.addTask("Deleting \"" + f.getName() + "\"", task);
            f.getParent().getChildren().remove(f);
        }
    }

    public Updater getUpdater() {
        return updater;
    }

    public DirectoryItem getRoot() {
        DirectoryItem root = new DirectoryItem(this, "root", Statics.ROOT_MESSAGE_ID, true);
        setChildren(root, discord.getRoot().getContent());
        return root;
    }

    public void setDirectoryChildren(DirectoryItem dir) {
        setChildren(dir, discord.treeGet(dir.getId() + "").getContent());
    }

    public void setChildren(DirectoryItem dir, String msgContent) {
        String[] arr = msgContent.split("\\?", -1);
        String dirs = arr[1];
        String files = arr[2];
        dir.getChildren().clear();
        if (!dirs.isEmpty()) {
            for (String id : dirs.split("/")) {
                String[] content = discord.treeGet(id).getContent().split("\\?", -1);
                DirectoryItem child = new DirectoryItem(this, content[0], id);
                dir.getChildren().add(child);
            }
        }
        if (!files.isEmpty()) {
            for (String id : files.split("/")) {
                String[] content = discord.treeGet(id).getContent().split("\\?", -1);
                FileItem child = new FileItem(this, content[0], id);
                dir.getChildren().add(child);
            }
        }
    }

    public void upload(File file, DirectoryItem parent) {
        parent.setExpanded(true);
        parent.setGraphic(new ImageView(Statics.IMG_LOADING_FOLDER));
        if (file.isDirectory()) {
            Task<DirectoryItem> task = tm.createFolder(parent, file.getName());
            task.setOnSucceeded((event) -> {
                try {
                    DirectoryItem newDir = task.get();
                    parent.setGraphic(new ImageView(Statics.IMG_FOLDER));
                    newDir.setGraphic(new ImageView(Statics.IMG_FOLDER));
                    for (File f : file.listFiles()) {
                        upload(f, newDir);
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    Logger.getLogger(FilesWrk.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            updater.addTask("Creating \"" + file.getName() + "\" folder", task);
        } else if (file.exists()) {
            Task<Void> task = tm.upload(file, parent);
            task.setOnSucceeded((event) -> {
                parent.setGraphic(new ImageView(Statics.IMG_FOLDER));
            });
            updater.addTask("Uploading \"" + file.getName() + "\"", task);
        }
    }
}
