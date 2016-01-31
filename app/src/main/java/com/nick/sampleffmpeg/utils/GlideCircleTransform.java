package com.nick.sampleffmpeg.utils;

/**
 * Created by Admin on 1/31/2016.
 */import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

public class GlideCircleTransform extends BitmapTransformation {
    public GlideCircleTransform(Context context) {
        super(context);
    }
    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap source, int outWidth, int outHeight) {
        return GlideImageUtils.getCircularBitmapImage(source);
    }
    @Override
    public String getId() {
        return "Glide_Circle_Transformation";
    }
}