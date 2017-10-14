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

    public Task createFolder(Directory newDir) {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateTitle("Creating \"" + newDir.getName() + "\" folder");
                updateProgress(0, 100);
                Message m = discord.treeGet(newDir.getParent().getCompleteId() + "");
                updateProgress(50, 100);
                String[] content = m.getContent().split("\\?", -1);
                String dirs = content[1];
                dirs += newDir.getId() + "/";
                m.editMessage(content[0] + "?" + dirs + "?" + content[2]).complete();
                updateProgress(100, 100);
                return null;
            }
        };
    }

    public Task deleteFolder(Directory dir) {
        return new Task<Void>() {

            private int progress = 0;
            private int max = dir.getSize() + 2;

            @Override
            protected Void call() throws Exception {
                updateTitle("Deleting \"" + dir.getName() + "\" folder");
                updateProgress(progress, max);
                deleteCascade(dir);
                Message m = discord.treeGet(dir.getParent().getCompleteId() + "");
                updateProgress(progress + 1, max);
                String[] content = m.getContent().split("\\?", -1);
                content[1] = content[1].replace(dir.getId() + "/", "");
                m.editMessage(content[0] + "?" + content[1] + "?" + content[2]).complete();
                return null;
            }

            private void deleteCascade(File f) {
                if (f.isDirectory()) {
                    for (File child : ((Directory) f).getChildren()) {
                        deleteCascade(child);
                    }
                }
                discord.treeGet(f.getCompleteId() + "").delete().complete();
                progress++;
                updateProgress(progress, max);
            }
        };
    }
}
