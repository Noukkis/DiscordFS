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
package discordfs.helpers;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jordan Vesy
 */
public final class PropertiesManager {

    private static final Properties props = new Properties();
    private static final File CONFIG_FILE = new File("config/config.properties");

    private PropertiesManager() {
    }

    public static void init() {
        try {
            props.load(new FileReader(CONFIG_FILE));
            Statics.BOT_TOKEN = props.getProperty("botToken");
            Statics.FILES_CHAN_ID = Long.parseLong(props.getProperty("filesChanID"));
            Statics.TREE_CHAN_ID = Long.parseLong(props.getProperty("treeChanID"));
            String rmid = props.getProperty("rootMessageID");
            Statics.ROOT_MESSAGE_ID = rmid != null ? rmid : "000";
        } catch (IOException ex) {
            Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void setRootMessageID(String id) {
        props.setProperty("rootMessageID", id);
        Statics.ROOT_MESSAGE_ID = id;
        save();
    }

    public static void save() {
        try {
            props.store(new FileWriter(CONFIG_FILE), "");
        } catch (IOException ex) {
            Logger.getLogger(PropertiesManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
