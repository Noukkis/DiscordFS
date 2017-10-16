package discordfs.helpers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jordan Vesy
 */
public final class Statics {

    public static final int MAX_FILE_SIZE = 8000000;
    public static final String IMG_FILE = "/images/file.png";
    public static final String IMG_FOLDER = "/images/folder.png";
    public static final String IMG_LOADING_FOLDER = "/images/loadingFolder.png";

    public static String BOT_TOKEN;
    public static long TREE_CHAN_ID;
    public static long FILES_CHAN_ID;
    public static String ROOT_MESSAGE_ID;

    public static byte[][] splitFile(File f, int size) {
        try {
            byte[] data = Files.readAllBytes(f.toPath());
            int length = data.length;
            byte[][] res = new byte[(length + size - 1) / size][];
            int resIndex = 0;
            int stopIndex = 0;

            for (int startIndex = 0; startIndex + size <= length; startIndex += size) {
                stopIndex += size;
                res[resIndex++] = Arrays.copyOfRange(data, startIndex, stopIndex);
            }

            if (stopIndex < length) {
                res[resIndex] = Arrays.copyOfRange(data, stopIndex, length);
            }

            return res;
        } catch (IOException ex) {
            Logger.getLogger(Statics.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static byte[] concat(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;
        byte[] c = new byte[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private Statics() {
    }
}
