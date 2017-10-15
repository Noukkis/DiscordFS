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

import discordfs.beans.Directory;
import discordfs.beans.File;
import discordfs.helpers.PropertiesManager;
import discordfs.helpers.Statics;
import java.util.Properties;
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

    public Directory createFolder(File f, String name) {
        Directory parent = f.isDirectory() ? (Directory) f : f.getParent();
        String id = discord.treeSend(name + "??");
        Directory newFolder = new Directory(parent, name, Long.parseLong(id));
        parent.getChildren().add(newFolder);
        Task<Void> task = tm.createFolder(newFolder);
        updater.addTask(task);
        return newFolder;
    }

    public void download(File f) {
    }

    public void delete(File f) {
        if (f.isDirectory()) {
            Directory dir = (Directory) f;
            Task<Void> task = tm.deleteFolder(dir);
            updater.addTask(task);
            if (f.isRoot()) {
                PropertiesManager.setRootMessageID(discord.treeSend("??"));
            } else {
                f.getParent().getChildren().remove(f);
            }
        }
    }

    public Updater getUpdater() {
        return updater;
    }

    public Directory getRoot() {
        Directory root = new Directory(null, "", Statics.ROOT_MESSAGE_ID, true);
        setChildren(root, discord.getRoot().getContent().split("\\?", -1)[1]);
        return root;
    }

    public void setDirectoryChildren(Directory dir) {
        setChildren(dir, discord.treeGet(dir.getCompleteId() + "").getContent().split("\\?", -1)[1]);
    }

    private void setChildren(Directory dir, String dirs) {
        if (!dirs.isEmpty()) {
            for (String id : dirs.split("/")) {
                long completeID = Long.parseLong(id) + dir.getCompleteId();
                String[] content = discord.treeGet(completeID + "").getContent().split("\\?", -1);
                Directory child = new Directory(dir, content[0], completeID);
                dir.getChildren().add(child);
            }
        }
    }
}
