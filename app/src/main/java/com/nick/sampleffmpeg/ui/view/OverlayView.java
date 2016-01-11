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
import java.util.ArrayList;
import java.util.List;

/**
 * Created by baebae on 12/25/15.
 */
public class OverlayView extends View {

    private Context appContext;
    private int width;
    private int height;
    private int currentVideoTime = 0;
    private TitleTimeLayout captionTimelineLayout = null;

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

    public void setCurrentVideoTime(int time) {
        currentVideoTime = time;
        invalidate();
    }

    public void setCaptionTimelayout(TitleTimeLayout layout) {
        this.captionTimelineLayout = layout;
    }

    public void updateOverlay(){
        overlayInformation = null;
        invalidate();
    }
    /**
     * create text Bitmap for overlay
     * @param overlay
     */
    private Bitmap createOverlayTextBitmap(OverlayBean.Overlay overlay, Bitmap backgroundBitmap) {
        Bitmap ret = null;
        if (backgroundBitmap != null) {
            int width = backgroundBitmap.getWidth();
            int height = backgroundBitmap.getHeight();

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

            ret = image;
        }

        return ret;
    }

    /**
     * resize bitmap with new widht, height
     * @param bm
     * @param newWidth
     * @param newHeight
     * @return
     */
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
    private Bitmap createBackgroundBitmap(OverlayBean.Overlay overlay) {
        Bitmap ret = null;
        if (overlay != null && overlay.backgroundImage.length() > 0) {
            String filePath = Constant.getApplicationDirectory() + MainApplication.getInstance().getTemplate().strDirectoryID + File.separator + overlay.backgroundImage;;
            Bitmap fullBitmap = FileUtils.getBitmapFromPNGFile(filePath);
            if (fullBitmap != null) {
                int newWidth = (int)(width * (overlay.width / 100.f));
                int newHeight = (int)(height * (overlay.height / 100.f));
                ret = getResizedBitmap(fullBitmap, newWidth, newHeight);
            }
        }

        return ret;
    }

    /**
     * draw overlay background , iamge
     * @param canvas
     * @param overlay
     */
    private void drawOverlayBackground(Canvas canvas, OverlayBean.Overlay overlay, Bitmap bitmapBackground, Bitmap bitmapText) {
        int x = (int)(width * (overlay.x / 100.f));
        int y = (int)(height * (overlay.y / 100.f));
        if (bitmapBackground != null) {
            canvas.drawBitmap(bitmapBackground, x, y, mBitmapPaint);
        }
        if (bitmapText != null) {
            canvas.drawBitmap(bitmapText, x, y, mBitmapPaint);
        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        viewBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        overlayInformation = MainApplication.getInstance().getTemplate();
        viewCanvas = new Canvas(viewBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        if (overlayInformation == null) {
            overlayInformation = MainApplication.getInstance().getTemplate();
        }
        canvas.drawBitmap(viewBitmap, 0, 0, mBitmapPaint);
        if (overlayInformation != null) {
            if (overlayInformation.brandLogo != null) {
                Bitmap backgroundBitmap = createBackgroundBitmap(overlayInformation.brandLogo);
                drawOverlayBackground(canvas, overlayInformation.brandLogo, backgroundBitmap, null);
            }
            if (!isRecordingView && this.captionTimelineLayout != null) {
                ArrayList<ChildTextTimelineLayout> captions = this.captionTimelineLayout.getTimelineTitlesInformation();
                for (int i = 0; i < captions.size(); i ++) {
                    ChildTextTimelineLayout caption = captions.get(i);

                    if (caption.getStartTime() <= currentVideoTime && caption.getEndTime() > currentVideoTime) {

                        OverlayBean.Overlay overlay = caption.getCaptionOverlay();
                        if (overlay != null) {
                            Bitmap backgroundBitmap = createBackgroundBitmap(overlay);
                            Bitmap textBitmap = createOverlayTextBitmap(overlay, backgroundBitmap);
                            drawOverlayBackground(canvas, overlay, backgroundBitmap, textBitmap);
                        }
                    }
                }
            }

        }
    }
}
