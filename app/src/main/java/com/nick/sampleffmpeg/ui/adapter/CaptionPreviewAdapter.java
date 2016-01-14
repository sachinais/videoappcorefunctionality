package com.nick.sampleffmpeg.ui.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.OverlayBean;

/**
 * Created by baebae on 1/12/16.
 */
public class CaptionPreviewAdapter extends BaseAdapter {
    private Activity mContext;
    private OverlayBean overlayBean = null;

    private View lastSelectedView = null;
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

    public View getSelectedView() {
        return lastSelectedView;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        View ret;
        if (convertView == null) {
            ret = mContext.getLayoutInflater().inflate(R.layout.caption_preview, null);
            ret.setTag(position);
        } else {
            ret = convertView;
        }

        int width = parent.getWidth();
        if (width > 0) {
            ret.setLayoutParams(new GridView.LayoutParams(width / 4 - 10, width / 4 - 10));
        }

        final OverlayBean.Overlay overlay = (OverlayBean.Overlay)getItem(position);
        overlay.customObject = ret;

        ret.findViewById(R.id.backgroundView).setBackgroundColor(overlay.backgroundColor);
        ((TextView)ret.findViewById(R.id.textView)).setTextColor(overlay.color);

        if (lastSelectedView == null && position == 0) {
            ret.setBackgroundColor(0xFF000000);
            lastSelectedView = ret;
        }

        ret.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i < overlayBean.captions.size(); i ++) {
                    View view = (View)(overlayBean.captions.get(i).customObject);
                    if (view != null) {
                        view.setBackgroundColor(0x00000000);
                    }
                }
                int index = (int)(v.getTag());
                if (index < overlayBean.captions.size()) {
                    View view = (View)(overlayBean.captions.get(index).customObject);
                    view.setBackgroundColor(0xFF000000);
                }
            }
        });
        return ret;
    }
}