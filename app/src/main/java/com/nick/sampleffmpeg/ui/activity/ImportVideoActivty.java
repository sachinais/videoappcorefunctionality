package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FontTypeface;
import com.nick.sampleffmpeg.utils.VideoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by Vindhya Pratap on 1/19/2016.
 */
public class ImportVideoActivty extends BaseActivity {
    //set constants for MediaStore to query, and show videos
    private final static Uri MEDIA_EXTERNAL_CONTENT_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
    private final static String _ID = MediaStore.Video.Media._ID;
    private final static String MEDIA_DATA = MediaStore.Video.Media.DATA;
    //flag for which one is used for images selection
    private GridView _gallery;
    private Cursor _cursor;
    private int _columnIndex;
    private Uri _contentUri;
    String filename, finalPath;
    int flag = 0;
    private Dialog optionDialog;
    String[] projection = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE
    };
    private int[] _videosId;
    String selection =  MediaStore.Files.FileColumns.MEDIA_TYPE + "="
            + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    final int ADD_CLIP_REQUEST=1000;
    protected Context _context;
    final int SELECT_PICTURE =100;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* _context = getApplicationContext();
        setContentView(R.layout.activity_import_video);
        _gallery = (GridView) findViewById(R.id.gridView);
        _contentUri = MEDIA_EXTERNAL_CONTENT_URI;
        initVideosId();
        setGalleryAdapter();
        findViewById(R.id.llLeftArrow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });*/

       /* Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("video*//*");

        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_PICTURE );*/
        Intent intent = new Intent();
        intent.setType("video/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Video"),SELECT_PICTURE);


    }
    private void setGalleryAdapter() {
        _gallery.setAdapter(new VideoGalleryAdapter(_context));
        _gallery.setOnItemClickListener(_itemClickLis);
        flag = 1;
    }
    private AdapterView.OnItemClickListener _itemClickLis = new AdapterView.OnItemClickListener()
    {
        @SuppressWarnings({ "deprecation", "unused", "rawtypes" })
        public void onItemClick(AdapterView parent, View v, int position, long id)
        {

            _columnIndex = _cursor.getColumnIndex(MEDIA_DATA);
            // Lets move to the selected item in the cursor
            _cursor.moveToPosition(position);
            // And here we get the filename
            filename = _cursor.getString(_columnIndex);
            if (VideoUtils.getVideoLength(filename) < 5) {
                showAlertDialog("Be sure your video is least 5 seconds long before importing.");
                return;
            }

            try{
                String s = filename.substring(filename.lastIndexOf(".") + 1);
                finalPath = destinationFilePath(s).getAbsolutePath();
                copyDirectory(new File(filename), new File(finalPath));
            }catch (Exception e){
                e.printStackTrace();
            }

            Constant.setSourceVideo(finalPath);

            int recordingTime = VideoUtils.getVideoLength(finalPath);
            if (recordingTime > 15.f) {
                Constant.updateTimeUnit(2);
            }

            if (recordingTime > 25) {
                Constant.updateTimeUnit(3);
            }

            if (recordingTime > 40) {
                Constant.updateTimeUnit(4);
            }
            ImportVideoActivty.this.finish();
            showActivity(EditingVideoActivity.class, finalPath);

            //*********** You can do anything when you know the file path :-)
            showToast(finalPath);




      }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();
                filename = getPath(this, selectedImageUri);
                if (filename != null) {
                    Constant.setSourceVideo(filename);

                    int recordingTime = VideoUtils.getVideoLength(filename);
                    int videoWidth = VideoUtils.getVideoWidth(filename);
                    int videoHeight = VideoUtils.getVideoHeight(filename);

                    Constant.setVideoSize(videoWidth, videoHeight);
                    if (recordingTime > 15.f) {
                        Constant.updateTimeUnit(2);
                    }

                    if (recordingTime > 25) {
                        Constant.updateTimeUnit(3);
                    }

                    if (recordingTime > 40) {
                        Constant.updateTimeUnit(4);
                    }
                    ImportVideoActivty.this.finish();
                    showActivity(EditingVideoActivity.class, filename);
                    return;
                }
            }
        }
        showActivity(RecordingVideoActivity.class, null);
        finish();
    }

    private void showAccountAlertDialog(String title, String message) {
        try {


            optionDialog = new Dialog(ImportVideoActivty.this);
            optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            optionDialog.setContentView(R.layout.select_video);
            optionDialog.setCancelable(false);
            optionDialog.setCanceledOnTouchOutside(false);
            ((TextView) optionDialog.findViewById(R.id.textView1)).setText(title);
            ((TextView) optionDialog.findViewById(R.id.textView2)).setText(message);
            ((TextView) optionDialog.findViewById(R.id.retry)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionDialog.dismiss();
                    //finish();

                    Intent intent = new Intent();
                    intent.setType("video/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Video"), SELECT_PICTURE);

                }
            });
            ((TextView) optionDialog.findViewById(R.id.txt_caption)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionDialog.dismiss();
                    showActivity(RecordingVideoActivity.class, null);
                    finish();

                }
            });
            optionDialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }






    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public File destinationFilePath(String extention){

        String dir= Constant.getApplicationDirectory()+"ImportedFiles";
        String filemedia = System.currentTimeMillis()+"."+extention;
        File file = new File(dir,filemedia);
        return file  ;
    }


    // If targetLocation does not exist, it will be created.
    public void copyDirectory(File sourceLocation , File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists() && !targetLocation.mkdirs()) {
                throw new IOException("Cannot create dir " + targetLocation.getAbsolutePath());
            }

            String[] children = sourceLocation.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            // make sure the directory we plan to store the recording in exists
            File directory = targetLocation.getParentFile();
            if (directory != null && !directory.exists() && !directory.mkdirs()) {
                throw new IOException("Cannot create dir " + directory.getAbsolutePath());
            }

            InputStream in = new FileInputStream(sourceLocation);
            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }
    }


    private void showAlertDialog(String message) {
        try {


            optionDialog = new Dialog(ImportVideoActivty.this);
            optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            optionDialog.setContentView(R.layout.five_sec_video);

            ((TextView) optionDialog.findViewById(R.id.textView1)).setText("Oops, we can't let you do that");
            ((TextView) optionDialog.findViewById(R.id.textView2)).setText(message);

           // ((TextView)optionDialog.findViewById(R.id.textView1)).setTypeface(FontTypeface.getTypeface(ImportVideoActivty.this, AppConstants.FONT_SUFI_REGULAR));
        //    ((TextView)optionDialog.findViewById(R.id.textView2)).setTypeface(FontTypeface.getTypeface(ImportVideoActivty.this, AppConstants.FONT_SUFI_REGULAR));

            ((TextView) optionDialog.findViewById(R.id.buttonYes)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    optionDialog.dismiss();

                }
            });
            optionDialog.show();


        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @SuppressWarnings("deprecation")
    private void initVideosId() {
        try
        {
            //Here we set up a string array of the thumbnail ID column we want to get back
            String [] proj={_ID};
            // Now we create the cursor pointing to the external thumbnail store
            Uri queryUri = MediaStore.Files.getContentUri("external");

            CursorLoader cursorLoader = new CursorLoader(
                    this,
                    queryUri,
                    projection,
                    selection,
                    null, // Selection args (none).
                    MediaStore.Files.FileColumns.DATE_ADDED + " DESC" // Sort order.
            );

             _cursor = cursorLoader.loadInBackground();
            int count= _cursor.getCount();
            // We now get the column index of the thumbnail id
            _columnIndex = _cursor.getColumnIndex(_ID);
            //initialize
            _videosId = new int[count];
            //move position to first element
            _cursor.moveToFirst();
            for(int i=0;i<count;i++)
            {
                int id = _cursor.getInt(_columnIndex);
                //
                _videosId[i]= id;
                //
                _cursor.moveToNext();
                //
            }
        }catch(Exception ex)
        {
            showToast(ex.getMessage().toString());
        }

    }
    protected void showToast(String msg)
    {
       // Toast.makeText(_context, msg, Toast.LENGTH_LONG).show();
    }

    //
    private class VideoGalleryAdapter extends BaseAdapter
    {        LayoutInflater inflator;
        private ViewHolder viewHolder;

        public VideoGalleryAdapter(Context c)
        {
            _context = c;
        }
        public int getCount()
        {
            return _videosId.length;
        }
        public Object getItem(int position)
        {
            return position;
        }
        public long getItemId(int position)
        {
            return position;
        }
        public View getView(int position, View convertView, ViewGroup parent)
        {


            if (convertView == null) {
                inflator = (LayoutInflater) ImportVideoActivty.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflator.inflate(R.layout.import_video_imagevide, null);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {

                viewHolder = (ViewHolder) convertView.getTag();
            }

            viewHolder.iv_ImportVideo.setImageBitmap(getImage(_videosId[position]));
            //imgVw.setLayoutParams(new GridView.LayoutParams(200,200));
         //  imgVw.setPadding(8, 8, 8, 8);
            /*ImageView imgVw= new ImageView(_context);;
            try
            {
                if(convertView!=null)
                {
                    imgVw= (ImageView) convertView;
                }
                imgVw.setImageBitmap(getImage(_videosId[position]));
                imgVw.setLayoutParams(new GridView.LayoutParams(200,200));
                imgVw.setPadding(8, 8, 8, 8);
            }
            catch(Exception ex)
            {
                System.out.println("ImportVideoActivty:getView()-135: ex " + ex.getClass() +", "+ ex.getMessage());
            }*/
            return convertView;
        }

        // Create the thumbnail on the fly
        private Bitmap getImage(int id) {
            Bitmap thumb = MediaStore.Video.Thumbnails.getThumbnail(
                    getContentResolver(),
                    id, MediaStore.Video.Thumbnails.MICRO_KIND, null);
            return thumb;
        }

    }
    static class ViewHolder {
        private ImageView iv_ImportVideo;

        ViewHolder(View view) {

            iv_ImportVideo = (ImageView) view.findViewById(R.id.iv_ImportVideo);


        }
    }

}
