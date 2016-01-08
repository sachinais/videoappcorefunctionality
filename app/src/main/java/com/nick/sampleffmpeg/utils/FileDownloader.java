package com.nick.sampleffmpeg.utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Vindhya Pratap on 1/7/2016.
 */
public class FileDownloader {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private Button startBtn;
    private ProgressDialog mProgressDialog;
    private String url;
    private  Context context;
    private  String fileName;
    private  String _directoryName;
    public FileDownloader(Context context, String url, String fileName, String _directoryName){
        this.url = url;
        this.context = context;
        this.fileName = fileName;
        this._directoryName = _directoryName;
    }

    public void startDownload() {
    new DownloadFileAsync().execute(url);
}

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                return mProgressDialog;
            default:
                return null;
        }
    }

    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("Downloading file..");
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {

                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                InputStream input = new BufferedInputStream(url.openStream());

                OutputStream output = new FileOutputStream(getFilePath(_directoryName)+"/"+fileName+".zip");

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

                String unzipLocation = getFilePath(_directoryName)+"/";
                String zipFile = getFilePath(_directoryName)+"/"+fileName+".zip";

                Decompress d = new Decompress(zipFile, unzipLocation);
                d.unzip();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }
        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC",progress[0]);
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            mProgressDialog.dismiss();
        }
    }
    private String getFilePath(String _directorName){
        File folder = new File(Environment.getExternalStorageDirectory() + "/VideoEditorApp");
        File file2 = null;
        boolean success = true;
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (!folder.exists()) {
                folder.mkdir();
            }
            file2 = new File(folder.getAbsolutePath(),_directorName);
            if(!file2.exists()){
                file2.mkdir();

            }

        }
        return file2.getAbsolutePath();
    }
}
