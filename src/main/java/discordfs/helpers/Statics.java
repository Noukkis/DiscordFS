package discordfs.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jordan Vesy
 */
public final class Statics {
           
    public static String BOT_TOKEN;
    public static long TREE_CHAN_ID;
    public static long FILES_CHAN_ID;
    public static long ROOT_MESSAGE_ID;

    private Statics() {
    }

    public static List<String> splitString(String s, int size) {
        List<String> res = new ArrayList<String>();
        int len = s.length();
        for (int i = 0; i < len; i += size) {
            res.add(s.substring(i, Math.min(len, i + size)));
        }
        return res;
    }
}
