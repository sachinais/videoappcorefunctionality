package com.nick.sampleffmpeg.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.OverlayBean;
import com.nick.sampleffmpeg.utils.FileUtils;

import java.io.File;
import java.util.List;

/**
 * Created by baebae on 12/25/15.
 */
public class OverlayView extends View {

    private Context appContext;
    private int width;
    private int height;
    private Bitmap viewBitmap;
    private Canvas viewCanvas;
    private Paint mBitmapPaint;
    private OverlayBean overlayInformation = MainApplication.getInstance().getTemplate();

    private void init()
    {
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    private boolean isRecordingView = true;

    public OverlayView(Context context)
    {
        super(context);
        this.appContext = context;
        init();
    }

    public OverlayView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        this.appContext = context;
        init();
    }

    public void setRecordingView(boolean flag) {
        this.isRecordingView = flag;
    }

    public void setJobTitle(String strTitle) {
        if (overlayInformation == null) {
            overlayInformation = MainApplication.getInstance().getTemplate();
        }

        if (overlayInformation != null && overlayInformation.name != null ) {
            overlayInformation.name.text = strTitle;
            createOverlayTextBitmap(overlayInformation.name);
        }
    }

    /**
     * create text Bitmap for overlay
     * @param overlay
     */
    private void createOverlayTextBitmap(OverlayBean.Overlay overlay) {
        overlay.bitmapText = null;
        if (overlay.bitmapBackground != null) {
            int width = overlay.bitmapBackground.getWidth();
            int height = overlay.bitmapBackground.getHeight();

            int margin = (int) ((overlay.marginLeft / 100) * width) ;
            int fontSize = (int) (((double)overlay.fontSize / 100.0) * height) ;
            Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(image);

            TextPaint mTextPaint = new TextPaint();
            mTextPaint.setColor(overlay.color);
            mTextPaint.setTextSize(fontSize);
            mTextPaint.setAntiAlias(true);
            StaticLayout mTextLayout = new StaticLayout(overlay.text, mTextPaint, canvas.getWidth(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

            canvas.save();

            canvas.translate(margin, (height - mTextLayout.getHeight()) / 2);
            mTextLayout.draw(canvas);
            canvas.restore();

            overlay.bitmapText = image;
        }
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    /**
     * resize overlay background bitmap
     * @param overlay
     */
    private void resizeOverlayBackgroundBitmap(OverlayBean.Overlay overlay) {
        if (overlay.bitmapBackground != null) {
            int newWidth = (int)(width * (overlay.width / 100.f));
            int newHeight = (int)(height * (overlay.height / 100.f));
            overlay.bitmapBackground = getResizedBitmap(overlay.bitmapBackground, newWidth, newHeight);
        }
    }

    /**
     * draw overlay background , iamge
     * @param canvas
     * @param overlay
     */
    private void drawOverlayBackground(Canvas canvas, OverlayBean.Overlay overlay) {
        int x = (int)(width * (overlay.x / 100.f));
        int y = (int)(height * (overlay.y / 100.f));
        if (overlay.bitmapBackground != null) {
            canvas.drawBitmap(overlay.bitmapBackground, x, y, mBitmapPaint);
        }
        if (overlay.bitmapText != null) {
            canvas.drawBitmap(overlay.bitmapText, x, y, mBitmapPaint);
        }
    }

    /**
     * init overlay objects (resize width , height based on overlay view size)
     */
    private void initOverlayObject() {
        overlayInformation = MainApplication.getInstance().getTemplate();
        if (overlayInformation != null) {
            if (overlayInformation.brandLogo != null) {
                resizeOverlayBackgroundBitmap(overlayInformation.brandLogo);
            }
            if (!isRecordingView) {
                if (overlayInformation.contact != null) {
                    resizeOverlayBackgroundBitmap(overlayInformation.contact);
                }

                if (overlayInformation.name != null) {
                    resizeOverlayBackgroundBitmap(overlayInformation.name);
                    createOverlayTextBitmap(overlayInformation.name);
                }

                for (int i = 0; i < overlayInformation.captions.size(); i++) {
                    resizeOverlayBackgroundBitmap(overlayInformation.captions.get(i));
                }
            }
        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        initOverlayObject();
        viewCanvas = new Canvas(viewBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (overlayInformation == null) {
            initOverlayObject();
        }
        canvas.drawBitmap(viewBitmap, 0, 0, mBitmapPaint);
        if (overlayInformation != null) {
            if (overlayInformation.brandLogo != null) {
                drawOverlayBackground(canvas, overlayInformation.brandLogo);
            }
            if (!isRecordingView) {
                if (overlayInformation.contact != null) {
                    drawOverlayBackground(canvas, overlayInformation.contact);
                }

                if (overlayInformation.name != null) {
                    drawOverlayBackground(canvas, overlayInformation.name);
                }

                for (int i = 0 ; i < overlayInformation.captions.size(); i ++) {
                    drawOverlayBackground(canvas, overlayInformation.captions.get(i));
                }
            }

        }
    }
}
