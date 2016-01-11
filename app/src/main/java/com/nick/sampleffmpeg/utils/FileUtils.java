package com.nick.sampleffmpeg.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaScannerConnection;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;

import com.nick.sampleffmpeg.ui.view.OverlayView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by baebae on 12/23/15.
 */
public class FileUtils {

    private static final String ASSET_COPYING_FLAG_SUFFIX = "AssetCopyingDone";
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String PATH_SEPARATOR = System.getProperty("path.separator");
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Delete all files in folder
     * @param fileOrDirectory
     */
    public synchronized static void DeleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                DeleteRecursive(child);

        fileOrDirectory.delete();
    }

    /**
     * Delete all files in folder path
     * @param path
     */
    public synchronized static void DeleteFolder(String path){
        File directory = new File(path);
        File to = new File(directory.getAbsolutePath() + System.currentTimeMillis());
        directory.renameTo(to);
        DeleteRecursive(to);
    }

    /**
     * copy files from asset to target path
     * @param context application context
     * @param destDirPath target dir path
     * @throws IOException
     */
    public static void copyAssets(Context context,  String destDirPath) throws IOException {
        SharedPreferences prefs = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        List<String> paths = new ArrayList<String>();
        copyAssets(context.getAssets(), "app_files", destDirPath, paths, false);
        MediaScannerConnection.scanFile(context, paths.toArray(new String[paths.size()]), null, null);
    }

    /**
     * combine path with file separator
     * @param folders folder names
     * @return combined path using folder names
     */
    public static String combinePath(String... folders) {
        String path = "";
        for (String folder : folders) {
            path = path.concat(FILE_SEPARATOR).concat(folder);
        }
        return path;
    }

    /**
     * copy files from asses in application to dest folder
     * @param manager
     * @param assetDir
     * @param destDirPath
     * @param paths
     * @param isRoot
     * @throws IOException
     */
    private static void copyAssets(AssetManager manager, String assetDir, String destDirPath, List<String> paths, boolean isRoot) throws IOException {
        String[] filePaths = manager.list(assetDir);
        for (String filePath : filePaths) {
            String src = isRoot ? filePath : combinePath(assetDir, filePath).substring(1);
            File destDir = isRoot ? new File(destDirPath) : new File(destDirPath, assetDir);
            if (!destDir.exists()) {
                if (!destDir.mkdirs()) {
                    throw new IOException("Cannot create file: " + destDir.getAbsolutePath());
                }
            }
            String dest = combinePath(destDir.getAbsolutePath(), filePath).substring(1);
            InputStream in = null;
            OutputStream out = null;
            try {
                in = manager.open(src);
                out = new FileOutputStream(dest);
                copy(in, out);
                paths.add(dest);
            } catch (FileNotFoundException e) {
                // Must be a directory - ignore and continue.
            } finally {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            }
            copyAssets(manager, src, destDirPath, paths, false);
        }
    }

    /**
     * copy file into another
     * @param source src file
     * @param destination dst file
     * @throws IOException
     */
    public static void copy(InputStream source, OutputStream destination) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = source.read(buffer)) != -1) {
            destination.write(buffer, 0, read);
        }
    }

    public static void createTitleCaptionPNG(String title, String filePath) {
        File file = new File(filePath);
        try {

            Rect rect = new Rect(0, 0, 1280, 150);

            Bitmap image = Bitmap.createBitmap(rect.width(), rect.height(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);

            int color = Color.argb(160, 1, 200, 245);

            Paint paint = new Paint();
            paint.setColor(color);

            canvas.drawRect(rect, paint);

            TextPaint mTextPaint=new TextPaint();
            mTextPaint.setColor(Color.argb(255, 255, 255, 255));
            mTextPaint.setTextSize(40);
            mTextPaint.setAntiAlias(true);
            StaticLayout mTextLayout = new StaticLayout(title, mTextPaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

            canvas.save();

            canvas.translate(100, 75 - mTextLayout.getHeight() / 2);
            mTextLayout.draw(canvas);
            canvas.restore();


            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream ostream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 10, ostream);
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public synchronized static Bitmap getBitmapFromPNGFile(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        return bitmap;
    }

    /**
     * check file is existing
     * @param fileName
     * @return existing status
     */
    public synchronized static boolean isExistFile(String fileName){
        File file = new File(fileName);
        if(file.exists())
            return true;
        return false;
    }
}
