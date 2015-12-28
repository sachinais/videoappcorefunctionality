package com.nick.sampleffmpeg.utils;

/**
 * Created by baebae on 12/23/15.
 */
public class StringUtils {

    /**
     * convert seconds into min:sec
     * @param time seconds
     * @param flagTwoDigit if flagTwoDigit == true, result format mm:ss, else ?min ?sec
     * @return
     */
    public static String getMinuteSecondString(int time, boolean flagTwoDigit) {
        String ret = "";
        int min = 0;
        int sec = 0;
        if (time < 60) {
            sec = time;
        } else {
            min = time / 60;
            sec = time % 60;
        }

        if (min < 10 && flagTwoDigit) {
            ret = "0";
        }
        ret = ret + Integer.toString(min);

        if (flagTwoDigit) {
            ret = ret + ":";
        } else {
            ret = ret + "min ";
        }
        if (sec < 10 && flagTwoDigit) {
            ret = ret + "0";
        }
        ret = ret + Integer.toString(sec);

        if (!flagTwoDigit) {
            ret = ret + "sec";
        }
        return ret;
    }
}
