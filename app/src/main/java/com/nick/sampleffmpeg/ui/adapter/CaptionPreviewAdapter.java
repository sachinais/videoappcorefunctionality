package com.nick.sampleffmpeg.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.OverlayBean;

import java.io.File;

/**
 * Created by baebae on 1/12/16.
 */
public class CaptionPreviewAdapter extends BaseAdapter {
    private Activity mContext;
    private OverlayBean overlayBean = null;

    private int selectedItem = -1;
    // Constructor
    public CaptionPreviewAdapter(Activity c) {
        mContext = c;
        overlayBean = MainApplication.getInstance().getTemplate();
    }

    public int getCount() {
        return overlayBean.captions.size();
    }

    public Object getItem(int position) {
        return overlayBean.captions.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(int index) {
        selectedItem = index;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View ret;
        OverlayBean.Overlay overlay = (OverlayBean.Overlay)getItem(position);
        if (convertView == null) {
            ret = mContext.getLayoutInflater().inflate(R.layout.caption_preview, null);
        } else {
            ret = convertView;
        }

        int width = parent.getWidth();
        if (width > 0) {
            ret.setLayoutParams(new GridView.LayoutParams(width / 4 - 10, width / 4 - 10));
        }

        ImageView overlayView = (ImageView)ret.findViewById(R.id.backgroundView);

        String filePath = Constant.getApplicationDirectory() + MainApplication.getInstance().getTemplate().strDirectoryID + File.separator + overlay.backgroundImage;;

        File imgFile = new  File(filePath);
        if(imgFile.exists()){
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            overlayView.setImageBitmap(myBitmap);
            overlayView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        ((TextView)ret.findViewById(R.id.txt_caption)).setText("Caption " + Integer.toString(position + 1));


        return ret;
    }
}