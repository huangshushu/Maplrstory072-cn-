package tools;

import java.util.Date;
import java.util.SimpleTimeZone;
import tools.packet.PacketHelper;

/**
 * Provides a suite of tools for manipulating Korean Timestamps.
 *
 * @author Frz
 * @since Revision 746
 * @version 1.0
 */
public class KoreanDateUtil {

    private final static int ITEM_YEAR2000 = -1085019342;
    private final static long REAL_YEAR2000 = 946681229830l;

    /**
     * Converts a Unix Timestamp into File Time
     *
     * @param realTimestamp The actual timestamp in milliseconds.
     * @return A 64-bit long giving a filetime timestamp
     */
    public static final long getTempBanTimestamp(final long realTimestamp) {
        // long time = (realTimestamp / 1000);//seconds
        return ((realTimestamp * 10000) + PacketHelper.FT_UT_OFFSET);
    }

    /**
     * Gets a timestamp for item expiration.
     *
     * @param realTimestamp The actual timestamp in milliseconds.
     * @return The Korean timestamp for the real timestamp.
     */
    public static final int getItemTimestamp(final long realTimestamp) {
        final int time = (int) ((realTimestamp - REAL_YEAR2000) / 1000 / 60); // convert to minutes
        return (int) (time * 35.762787) + ITEM_YEAR2000;
    }

    public static boolean isDST() {
        return SimpleTimeZone.getDefault().inDaylightTime(new Date());
    }

    public static long getFileTimestamp(long timeStampinMillis) {
        return getFileTimestamp(timeStampinMillis, false);
    }

    public static long getFileTimestamp(long timeStampinMillis, boolean roundToMinutes) {
        if (isDST()) {
            timeStampinMillis -= 3600000L;
        }
        timeStampinMillis += 12 * 60 * 60 * 1000;
        long time;
        if (roundToMinutes) {
            time = (timeStampinMillis / 1000 / 60) * 600000000;
        } else {
            time = timeStampinMillis * 10000;
        }
        return time + PacketHelper.FT_UT_OFFSET;
    }
}
