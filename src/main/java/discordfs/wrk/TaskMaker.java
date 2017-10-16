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
import discordfs.helpers.Statics;
import java.io.File;
import java.nio.file.Files;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Jordan Vesy
 */
public final class TaskMaker {

    private final DiscordWrk discord;
    private final FilesWrk wrk;

    public TaskMaker(FilesWrk wrk, DiscordWrk discord) {
        this.discord = discord;
        this.wrk = wrk;
    }

    public Task<DirectoryItem> createFolder(DirectoryItem parent, String name) {
        return new Task<DirectoryItem>() {
            @Override
            protected DirectoryItem call() throws Exception {
                updateProgress(0, 2);
                String id = discord.treeSend(name + "??");
                DirectoryItem newDir = new DirectoryItem(wrk, name, id);
                parent.getChildren().add(newDir);
                Message m = discord.treeGet(newDir.getParentDir().getId() + "");
                updateProgress(1, 2);
                String[] content = m.getContent().split("\\?", -1);
                String dirs = content[1];
                dirs += newDir.getId() + "/";
                m.editMessage(content[0] + "?" + dirs + "?" + content[2]).complete();
                updateProgress(2, 2);
                return newDir;
            }
        };
    }

    public Task delete(FileItem f, DirectoryItem parent) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateProgress(0, 2);
                Message m = discord.treeGet(parent.getId());
                updateProgress(1, 2);
                String content = m.getContent();
                content = content.replace(f.getId() + "/", "");
                m.editMessage(content).complete();
                updateProgress(2, 2);
                return null;
            }
        };
    }

    public Task upload(File file, DirectoryItem parent) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                byte[][] bytes = Statics.splitFile(file, Statics.MAX_FILE_SIZE);
                String firstID = discord.treeSend(file.getName() + "?" + bytes.length);
                int progress = 3;
                updateProgress(0, 1);
                Message treeMsg = discord.treeGet(firstID);
                int max = bytes.length + 5;
                updateProgress(3, max);
                String msg = "final";
                for (int i = 0; i < bytes.length; i++) {
                    byte[] aByte = bytes[bytes.length - 1 - i];
                    try {
                        msg = discord.filesSend(aByte, "part" + i, msg);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    progress++;
                    updateProgress(progress, max);
                }
                treeMsg.editMessage(treeMsg.getContent() + "?" + msg).complete();
                Message parentMsg = discord.treeGet(parent.getId());
                parentMsg.editMessage(parentMsg.getContent() + treeMsg.getId() + "/").complete();
                FileItem newFile = new FileItem(wrk, file.getName(), firstID);
                parent.getChildren().add(newFile);
                updateProgress(max, max);
                return null;
            }
        };
    }

    public Task<Void> download(File dir, FileItem fi) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                if (fi.isDirectory()) {
                    updateProgress(0, 3);
                    DirectoryItem di = (DirectoryItem) fi;
                    String content = discord.treeGet(di.getId()).getContent();
                    updateProgress(1, 3);
                    wrk.setChildren(di, content);
                    updateProgress(2, 3);
                    File newDir = new File(dir, fi.getName());
                    newDir.mkdir();
                    for (TreeItem<String> f : di.getChildren()) {
                        Platform.runLater(() -> wrk.download(newDir, (FileItem) f));
                    }
                    updateProgress(3, 3);
                } else {
                    updateProgress(0, 3);
                    String[] content = discord.treeGet(fi.getId()).getContent().split("\\?");
                    int max = Integer.parseInt(content[1]) * 2;
                    int progress = 1;
                    updateProgress(1, max);
                    if (max <= 2) {
                        File f = new File(dir, fi.getName());
                        Message m = discord.filesGet(content[2]);
                        m.getAttachments().get(0).download(f);
                        updateProgress(2, max);
                    } else {
                        File temp = new File(dir, "temp" + (long) (Math.random() * Long.MAX_VALUE));
                        temp.mkdir();
                        String msg = content[2];
                        while (!msg.equals("final")) {
                            File f = new File(temp, "part" + progress);
                            Message m = discord.filesGet(msg);
                            msg = m.getContent();
                            m.getAttachments().get(0).download(f);
                            progress++;
                            updateProgress(progress, max);
                        }
                        byte[] b = new byte[0];
                        for (File f : temp.listFiles()) {
                            b = Statics.concat(b, Files.readAllBytes(f.toPath()));
                            progress++;
                            updateProgress(progress, max);
                        }
                        Files.write(new File(dir, fi.getName()).toPath(), b);
                        for (File f : temp.listFiles()) {
                            f.delete();
                        }
                        temp.delete();
                    }
                }
                return null;
            }
        };

    }
}
