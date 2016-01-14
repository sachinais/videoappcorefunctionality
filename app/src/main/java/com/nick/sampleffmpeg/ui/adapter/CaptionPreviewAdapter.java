package com.nick.sampleffmpeg.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.OverlayBean;

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

        ret.findViewById(R.id.backgroundView).setBackgroundColor(overlay.backgroundColor);
        ((TextView)ret.findViewById(R.id.textView)).setTextColor(overlay.color);

        if (selectedItem == -1 && position == 0) {
            ret.findViewById(R.id.selected).setVisibility(View.VISIBLE);
            selectedItem = 0;
        }
        return ret;
    }
}