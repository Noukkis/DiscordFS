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

import discordfs.helpers.PropertiesManager;
import discordfs.helpers.Statics;
import java.io.File;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.exceptions.ErrorResponseException;

/**
 *
 * @author Jordan Vesy
 */
public class DiscordWrk {

    private final TextChannel treeChan;
    private final TextChannel filesChan;

    public DiscordWrk(JDA jda) {
        treeChan = jda.getTextChannelById(Statics.TREE_CHAN_ID);
        filesChan = jda.getTextChannelById(Statics.FILES_CHAN_ID);
        try {
            getRoot();
        } catch (ErrorResponseException e) {
            PropertiesManager.setRootMessageID(treeSend("??"));
        }
    }

    public String treeSend(String s) {
        return treeChan.sendMessage(s).complete().getId();
    }

    public String filesSend(byte[] data, String name, String msg) {
        return filesChan.sendFile(data, name, new MessageBuilder().append(msg).build()).complete().getId();
    }

    public Message treeGet(String id) {
       return treeChan.getMessageById(id).complete();
    }

    public Message filesGet(String id) {
       return filesChan.getMessageById(id).complete();
    }

    public Message getRoot() {
        return treeChan.getMessageById(Statics.ROOT_MESSAGE_ID).complete();
    }
}
