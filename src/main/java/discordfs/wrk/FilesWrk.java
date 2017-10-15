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

import discordfs.beans.DirectoryView;
import discordfs.beans.FileView;
import discordfs.helpers.PropertiesManager;
import discordfs.helpers.Statics;
import java.io.File;
import javafx.concurrent.Task;

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
        tm = new TaskMaker(discord);
    }

    public DirectoryView createFolder(FileView f, String name) {
        DirectoryView parent = f.isDirectory() ? (DirectoryView) f : f.getParent();
        String id = discord.treeSend(name + "??");
        DirectoryView newFolder = new DirectoryView(parent, name, id);
        parent.getChildren().add(newFolder);
        Task<Void> task = tm.createFolder(newFolder);
        updater.addTask(task);
        return newFolder;
    }

    public void download(File dir, FileView fw) {
        if (fw.isDirectory()) {
            DirectoryView dw = (DirectoryView) fw;
            setChildren(dw, discord.treeGet(dw.getId()).getContent());
            File newDir = new File(dir, fw.getName());
            newDir.mkdir();
            for (FileView f : dw.getChildren()) {
                download(newDir, f);
            }
        } else {
            Task<Void> task = tm.download(dir, fw);
            updater.addTask(task);
        }
    }

    public void delete(FileView f) {
        if (f.isRoot()) {
            PropertiesManager.setRootMessageID(discord.treeSend("??"));
        } else {
            Task<Void> task = tm.delete(f);
            updater.addTask(task);
            f.getParent().getChildren().remove(f);
        }
    }

    public Updater getUpdater() {
        return updater;
    }

    public DirectoryView getRoot() {
        DirectoryView root = new DirectoryView(null, "", Statics.ROOT_MESSAGE_ID, true);
        setChildren(root, discord.getRoot().getContent());
        return root;
    }

    public void setDirectoryChildren(DirectoryView dir) {
        setChildren(dir, discord.treeGet(dir.getId() + "").getContent());
    }

    private void setChildren(DirectoryView dir, String msgContent) {
        String[] arr = msgContent.split("\\?", -1);
        String dirs = arr[1];
        String files = arr[2];
        dir.getChildren().clear();
        if (!dirs.isEmpty()) {
            for (String id : dirs.split("/")) {
                String[] content = discord.treeGet(id).getContent().split("\\?", -1);
                DirectoryView child = new DirectoryView(dir, content[0], id);
                dir.getChildren().add(child);
            }
        }
        if (!files.isEmpty()) {
            for (String id : files.split("/")) {
                String[] content = discord.treeGet(id).getContent().split("\\?", -1);
                FileView child = new FileView(dir, content[0], id);
                dir.getChildren().add(child);
            }
        }
    }

    public FileView upload(File file, DirectoryView parent) {
        if (file.isDirectory()) {
            DirectoryView newDir = createFolder(parent, file.getName());
            for (File f : file.listFiles()) {
                upload(f, newDir);
            }
            return newDir;
        } 
        if (file.exists()) {
            String ID = discord.treeSend(file.getName() + "?");
            Task<Void> task = tm.upload(file, ID, parent);
            updater.addTask(task);
            FileView newFile = new FileView(parent, file.getName(), ID);
            parent.getChildren().add(newFile);
            return newFile;
        }
        return null;
    }
}
