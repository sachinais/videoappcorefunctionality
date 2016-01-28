package com.nick.sampleffmpeg.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

/**
 * Created by baebae on 1/27/16.
 */
public class BitmapUtils {
    public static Bitmap resizeBitmap(Bitmap originalImage, float width) {
        float originalWidth = originalImage.getWidth(), originalHeight = originalImage.getHeight();
        float height = width / (originalWidth / originalHeight);
        Bitmap background = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(background);
        float scale = width/originalWidth;
        float xTranslation = 0.0f, yTranslation = (height - originalHeight * scale)/2.0f;
        Matrix transformation = new Matrix();
        transformation.postTranslate(xTranslation, yTranslation);
        transformation.preScale(scale, scale);
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawBitmap(originalImage, transformation, paint);
        originalImage.recycle();;
        originalImage = null;
        return background;
    }
}
