package com.nick.sampleffmpeg.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Button;

import com.nick.sampleffmpeg.ui.activity.DownloadService;
import com.nick.sampleffmpeg.ui.activity.EditingVideoActivity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by Admin on 1/22/2016.
 */
public class TopDownloader {
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    private Button startBtn;
    private String url;
    private Context context;
    private  String extenstion;
    private  String _directoryName;
    String outPutFile;
    final int TIMEOUT_CONNECTION = 5000;//5sec
    private final int TIMEOUT_SOCKET = 30000;//30sec

    private EditingVideoActivity.TopVideoDownload topVideoDownload = null;
    public TopDownloader(Context context, String url, String extenstion, String _directoryName){
        this.url = url;
        this.context = context;
        this.extenstion = extenstion;
        this._directoryName = _directoryName;
    }

   /* public void startDownload(Runnable callback) {
        new DownloadFileAsync().execute(url);
        this.runnable = callback;
    }*/
    public void startDownload(EditingVideoActivity.TopVideoDownload topVideoDownload) {
        new DownloadFileAsync().execute(url);
        this.topVideoDownload = topVideoDownload;
    }
    class DownloadFileAsync extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

/*        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {

                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();
                Log.d("ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile);

                InputStream input = new BufferedInputStream(url.openStream());

                 outPutFile = getFilePath(_directoryName)+"/"+"TopVideo." +extenstion;

                OutputStream output = new FileOutputStream(outPutFile);

                byte data[] = new byte[1024 * 10];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();

               *//* String unzipLocation = getFilePath(_directoryName)+"/";
                String zipFile = getFilePath(_directoryName)+"/"+"TopAndTail";*//*

              *//*  //Decompress d = new Decompress(zipFile, unzipLocation);
                //d.unzip();*//*


            } catch (Exception e) {
                e.printStackTrace();
            }
            return outPutFile;

        }*/

        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {


                URL url = new URL(f_url[0]);
                long startTime = System.currentTimeMillis();
                URLConnection ucon = url.openConnection();

                //this timeout affects how long it takes for the app to realize there's a connection problem
                ucon.setReadTimeout(TIMEOUT_CONNECTION);
                ucon.setConnectTimeout(TIMEOUT_SOCKET);


                //Define InputStreams to read from the URLConnection.
                // uses 3KB download buffer
                InputStream is = ucon.getInputStream();
                BufferedInputStream inStream = new BufferedInputStream(is, 1024 * 5);
                outPutFile = getFilePath(_directoryName)+"/"+"TopVideo." +extenstion;
                FileOutputStream outStream = new FileOutputStream(outPutFile);
                byte[] buff = new byte[5 * 1024];

                //Read bytes (and store them) until there is nothing more to read(-1)
                int len;
                while ((len = inStream.read(buff)) != -1) {
                    outStream.write(buff, 0, len);
                }

                //clean up
                outStream.flush();
                outStream.close();
                inStream.close();



                return null;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return outPutFile;


    }


        protected void onProgressUpdate(String... progress) {
            Log.d("ANDRO_ASYNC",progress[0]);
        }

        @Override
        protected void onPostExecute(String unused) {


            if (topVideoDownload != null) {
                topVideoDownload.getTopVideoUrl(outPutFile);
            }
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
