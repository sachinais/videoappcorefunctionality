package com.nick.sampleffmpeg.utils;

import android.os.Environment;

import java.io.File;

/**
 * Created by Vindhya Pratap on 1/8/2016.
 */
public class AppFileUtils {

    /**
     * Get directory path
     * @param _templateDirId
     * @return
     */
    public static synchronized String getTemplateDirectoryPath(String _templateDirId){
        File folder = new File(Environment.getExternalStorageDirectory() + "/VideoEditorApp");
        File file2 = null;
        try{
            String state = Environment.getExternalStorageState();
            if (Environment.MEDIA_MOUNTED.equals(state)) {
                if (!folder.exists()) {
                    folder.mkdir();
                }
                file2 = new File(folder.getAbsolutePath(),_templateDirId);
                if(!file2.exists()){
                    file2.mkdir();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return file2.getAbsolutePath();
    }

}
