package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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

import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FontTypeface;
import com.nick.sampleffmpeg.utils.VideoUtils;


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
    String filename;
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

    protected Context _context;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _context = getApplicationContext();
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
        });

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
                showAlertDialog("Be sure you have select video file at least 5 seconds of footage");
                return;
            }
            ImportVideoActivty.this.finish();
            showActivity(EditingVideoActivity.class, null);

            //*********** You can do anything when you know the file path :-)
            showToast(filename);
      }
    };



    private void showAlertDialog(String message) {
        try {


            optionDialog = new Dialog(ImportVideoActivty.this);
            optionDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            optionDialog.setContentView(R.layout.alert_chooce_video);

            ((TextView) optionDialog.findViewById(R.id.textView1)).setText("Oopss, We can't let you do this");
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
