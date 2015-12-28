package com.nick.sampleffmpeg.utils;

import android.os.Environment;

import com.nick.sampleffmpeg.Define.Constant;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by baebae on 12/23/15.
 */
public class LogFile {

    /**
     * clear app.log in app directory
     */
    public static void clearLogText() {
        File logFile = new File(Constant.getLogFilePath());
        if (logFile.exists()) {
            logFile.delete();
        }
    }

    /**
     * append log into file in app directory
     * @param text logtext
     * @param element code line
     */
    public static void logText(String text, StackTraceElement element) {
        File logFile = new File(Constant.getLogFilePath());
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            SimpleDateFormat s = new SimpleDateFormat("hh:mm:ss");
            String format = s.format(new Date());
            String msg = format + " "  + text;
            if (element != null) {
                String fullClassName = element.getClassName();
                String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
                String methodName = element.getMethodName();
                int lineNumber = element.getLineNumber();
                msg = msg + " " + className + "." + methodName + "():" + lineNumber;
            }

            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(msg);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}