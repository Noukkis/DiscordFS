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
import discordfs.helpers.Statics;
import java.io.File;
import javafx.concurrent.Task;
import net.dv8tion.jda.core.entities.Message;

/**
 *
 * @author Jordan Vesy
 */
public final class TaskMaker {

    private final DiscordWrk discord;

    public TaskMaker(DiscordWrk discord) {
        this.discord = discord;
    }

    public Task createFolder(DirectoryView newDir) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateTitle("Creating \"" + newDir.getName() + "\" folder");
                updateProgress(0, 2);
                Message m = discord.treeGet(newDir.getParent().getId() + "");
                updateProgress(1, 2);
                String[] content = m.getContent().split("\\?", -1);
                String dirs = content[1];
                dirs += newDir.getId() + "/";
                m.editMessage(content[0] + "?" + dirs + "?" + content[2]).complete();
                updateProgress(2, 2);
                return null;
            }
        };
    }

    public Task delete(FileView f) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateTitle("Deleting \"" + f.getName() + "\"");
                updateProgress(0, 2);
                Message m = discord.treeGet(f.getParent().getId());
                updateProgress(1, 2);
                String content = m.getContent();
                content = content.replace(f.getId() + "/", "");
                m.editMessage(content).complete();
                updateProgress(2, 2);
                return null;
            }
        };
    }

    public Task upload(File file, String firstID, DirectoryView parent) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateTitle("Uploading \"" + file.getName() + "\"");
                int progress = 3;
                updateProgress(0, 1);
                Message treeMsg = discord.treeGet(firstID);
                byte[][] bytes = Statics.splitFile(file, Statics.MAX_FILE_SIZE);
                int max = bytes.length + 5;
                updateProgress(3, max);
                String msg = "final";
                for (int i = 0; i < bytes.length; i++) {
                    byte[] aByte = bytes[bytes.length - 1 - i];
                    msg = discord.filesSend(aByte, "part" + i, msg);
                    progress++;
                    updateProgress(progress, max);
                }
                treeMsg.editMessage(treeMsg.getContent() + msg).complete();
                Message parentMsg = discord.treeGet(parent.getId());
                parentMsg.editMessage(parentMsg.getContent() + treeMsg.getId() + "/").complete();
                updateProgress(max, max);
                return null;
            }
        };
    }

    public Task<Void> download(File dir, FileView fw) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateTitle("Downloading \"" + fw.getName() + "\"");
                updateProgress(0, 2);
                String id = discord.treeGet(fw.getId()).getContent().split("\\?")[1];
                updateProgress(1, 2);
                File f = new File(dir, fw.getName());
                discord.filesGet(id).getAttachments().get(0).download(f);
                updateProgress(2, 2);
                return null;
            }
        };

    }
}
