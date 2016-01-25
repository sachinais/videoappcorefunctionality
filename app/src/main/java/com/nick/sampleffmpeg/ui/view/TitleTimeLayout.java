package com.nick.sampleffmpeg.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.OverlayBean;
import com.nick.sampleffmpeg.ui.activity.EditingVideoActivity;
import com.nick.sampleffmpeg.ui.adapter.CaptionPreviewAdapter;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by baebae on 12/25/15.
 */
public class TitleTimeLayout extends RelativeLayout  implements View.OnTouchListener {
    private EditingVideoActivity parentActivity = null;
    private ChildTextTimelineLayout selectedItem = null;
    private boolean flagResizeLeft = false;
    private boolean flagResizeRight = false;
    private boolean flagMoving = false;

    private float movingOffset = 0;

    private float trimStart = 0f;
    private float trimEnd = 0f;

    private boolean flagDragging;
    private long dragStartTime;
    private float startDragX;
    private float lastDragX;

    private CaptionPreviewAdapter captionPreviewAdapter = null;

    final static private int TAP_DRIFT_TOLERANCE = 3;
    final static private int SINGLE_TAP_MAX_TIME = 175;

    public TitleTimeLayout(Context context) {
        super(context);
    }

    public TitleTimeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TitleTimeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }



    public void removeChildTitleLayouts() {
        ArrayList<ChildTextTimelineLayout>  timelineTitlesInformation = MainApplication.getTimelineTitlesInformation();
        for (int i = 0; i < timelineTitlesInformation.size(); i ++) {
            removeView(timelineTitlesInformation.get(i));
        }
        timelineTitlesInformation.clear();
    }

    private boolean checkTitleAlreadyExistInCurrentTimeLine(int currentVideoSeekPosition) {
        boolean ret = false;
        ArrayList<ChildTextTimelineLayout>  timelineTitlesInformation = MainApplication.getTimelineTitlesInformation();
        for (int i = 0; i < timelineTitlesInformation.size(); i ++) {
            float startTime = timelineTitlesInformation.get(i).getStartTime();
            float endTime = timelineTitlesInformation.get(i).getEndTime();
            if (currentVideoSeekPosition > (startTime - 0.5) * 1000 && currentVideoSeekPosition < (endTime + 0.5) * 1000) {
                return true;
            }
        }
        return ret;
    }

    public void setActivity(EditingVideoActivity activity) {
        this.parentActivity = activity;
    }

    /**
     * add title into title timeline area
     */
    private void addNewTitleInformation(String title, OverlayBean.Overlay overlay, int currentVideoSeekPosition) {
//        boolean flagAlreadyHaveTitle = checkTitleAlreadyExistInCurrentTimeLine(currentVideoSeekPosition);
//        if (flagAlreadyHaveTitle) {
//            parentActivity.showAlert(R.string.str_alert_title_information, R.string.str_title_already_exist, parentActivity.getString(R.string.str_alert_okay));
//        } else {
            float startTime = currentVideoSeekPosition / 1000.f;
            float endTime = startTime + Constant.TIMELINE_UNIT_SECOND;
            addNewTitleInformation(title, startTime, endTime, overlay, true);
//        }
    }

    public void addNewTitleInformation(String title, float startTime, float endTime, OverlayBean.Overlay overlay, boolean flagRemovable) {
        ArrayList<ChildTextTimelineLayout>  timelineTitlesInformation = MainApplication.getTimelineTitlesInformation();
        ChildTextTimelineLayout titleLayout = (ChildTextTimelineLayout)parentActivity.getLayoutInflater().inflate(R.layout.title_timeline_layout, null);
        titleLayout.setParentWidth(this.getWidth());
        titleLayout.setDisplayMetrics(parentActivity.getDisplayMetric());
        titleLayout.setInformation(startTime, endTime, title, System.currentTimeMillis(), overlay, flagRemovable);

        timelineTitlesInformation.add(titleLayout);
        addView(titleLayout);
        parentActivity.updateOverlayView(startTime);


        //showHintTextView();
    }

    /**
     * remove child title layout from timeline
     * @param layout
     */
    private void removeChildLayout(ChildTextTimelineLayout layout) {
        ArrayList<ChildTextTimelineLayout>  timelineTitlesInformation = MainApplication.getTimelineTitlesInformation();
        timelineTitlesInformation.remove(layout);
        removeView(layout);
        selectedItem = null;
    }
    /**
     * show/hide hint text on title timeline are (Tap to add title...)
     */
    private void showHintTextView() {
        ArrayList<ChildTextTimelineLayout>  timelineTitlesInformation = MainApplication.getTimelineTitlesInformation();
        if (timelineTitlesInformation.size() > 0) {
            findViewById(R.id.txt_hint_layout).setVisibility(View.GONE);
        } else {
            findViewById(R.id.txt_hint_layout).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        this.setOnTouchListener(this);

    }

    /**
     * get title layout object in timeline from position
     * @param position position
     * @return selected child layout
     */
    private ChildTextTimelineLayout getChildFromPosition(float position) {
        ArrayList<ChildTextTimelineLayout>  timelineTitlesInformation = MainApplication.getTimelineTitlesInformation();
        flagMoving = flagResizeLeft = flagResizeRight = false;
        for (int i = 0; i < timelineTitlesInformation.size(); i ++) {
            ChildTextTimelineLayout layout = timelineTitlesInformation.get(i);
            if (position > layout.getLayoutLeft() && position < layout.getLayoutRight()) {
                float leftMovingArea = layout.getLayoutLeft() + (layout.getLayoutRight() - layout.getLeft()) / 4.f;
                float rightMovingArea = layout.getLayoutLeft() + (layout.getLayoutRight() - layout.getLeft()) / 4.f * 3;
                if (position < leftMovingArea) {
                    flagResizeLeft = true;
                } else if (position > rightMovingArea){
                    flagResizeRight = true;
                } else {
                    movingOffset = layout.getLayoutLeft() - position;
                    flagMoving = true;
                }

                return layout;
            }
        }
        return null;
    }

    /**
     * check current position is already in other title area , it only used when user change start/end time of title
     * @param position moving position
     * @return if position is in other title area it will return true
     */
    private boolean checkConflictTitleArea(float position) {
        ArrayList<ChildTextTimelineLayout>  timelineTitlesInformation = MainApplication.getTimelineTitlesInformation();
        for (int i = 0; i < timelineTitlesInformation.size(); i ++) {
            ChildTextTimelineLayout layout = timelineTitlesInformation.get(i);
            if (position > layout.getLayoutLeft() -  parentActivity.getPixelFromDensity(20)
                    && position < layout.getLayoutRight() + parentActivity.getPixelFromDensity(20)) {
                if (layout.getChildTagID() != selectedItem.getChildTagID())
                    return true;
            }
        }
        return false;
    }

    private boolean firstTouch = false;

    @Override
    public boolean onTouch(View view, MotionEvent me) {
        // Only capture drag events if we start
        // Touch begin and get title from selected position
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            float seekVideoTime = ((float) me.getX() / (float) Constant.SP_PER_SECOND / parentActivity.getDisplayMetric().scaledDensity);

            if (seekVideoTime < trimEnd && seekVideoTime > trimStart) {
                selectedItem = null;
                lastDragX = startDragX = me.getX();
                selectedItem = getChildFromPosition(startDragX);

                if (selectedItem != null) {
                    flagDragging = true;
                    getParent().requestDisallowInterceptTouchEvent(true);
                }

                if (firstTouch && (SystemClock.elapsedRealtime() - dragStartTime) <= SINGLE_TAP_MAX_TIME * 2) {
                    firstTouch = false;

                    //parentActivity.setCurrentSeekTime(seekVideoTime * 1000);
                    /**
                     * if item is already selected on touch down event, it will show alert to delete current title.
                     * if no item is selected, add dialog will show
                     */

                    if (selectedItem != null) {
                        if (selectedItem.isRemovable()) {
                            parentActivity.showAlert(R.string.str_alert_title_information, R.string.str_delete_time_line,
                                    parentActivity.getString(R.string.str_yes), new Runnable() {
                                        @Override
                                        public void run() {
                                            removeChildLayout(selectedItem);
                                        }
                                    }, parentActivity.getString(R.string.str_no));
                        }
                    } else {
                        addNewTitleToTimeline((int)(seekVideoTime * 1000));
                    }
                } else {
                    firstTouch = true;
                    dragStartTime = SystemClock.elapsedRealtime();
                }
            }
            return true;
        }
        //Touch end event
        else if (me.getAction() == MotionEvent.ACTION_UP) {
            getParent().requestDisallowInterceptTouchEvent(false);
            flagDragging = false;
            return true;
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
            float x = me.getX();
            if (Math.abs(x - lastDragX) > 5) {
                if (flagDragging && (flagResizeLeft || flagResizeRight || flagMoving)) {
                    if (flagResizeLeft) {
                        selectedItem.setLayoutLeft(me.getX());
                    } else if (flagResizeRight) {
                        selectedItem.setLayoutRight(me.getX());
                    } else if (flagMoving) {
                        selectedItem.moveLayout(me.getX() + movingOffset);
                    }
                    updateCaptionLayoutForTrimView(selectedItem, false);
                }
            }
            lastDragX = x;
        }
        return true;
    }

    private GridView captionGridView;
    public interface SelectCaptionCallback {
        public abstract void onCaptionThemeSelected(OverlayBean.Overlay overlay);
    }
    private void showSelectCaptionDialog(final SelectCaptionCallback callback) {
        captionPreviewAdapter = new CaptionPreviewAdapter(parentActivity);
        View v = parentActivity.showViewContentDialog(R.layout.select_caption_dialog, parentActivity.getString(R.string.str_cancel));
        captionGridView = (GridView)v.findViewById(R.id.gridView);
        captionGridView.setAdapter(captionPreviewAdapter);
        captionGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                parentActivity.closeCurrentDialog();
                OverlayBean.Overlay overlay = MainApplication.getInstance().getTemplate().captions.get(position);
                callback.onCaptionThemeSelected(overlay);
            }
        });
    }
    /**
     * show dialog which require input title information.
     */
    private static EditText editTitle = null;

    private void addNewTitleToTimeline(final int time) {

        final OverlayBean template = MainApplication.getInstance().getTemplate();
        if (template.captions.size() == 0) {
            parentActivity.showAlert(R.string.str_alert_title_information, "There's no caption templates.", "OK");
        } else {
            showSelectCaptionDialog(new SelectCaptionCallback() {
                @Override
                public void onCaptionThemeSelected(final OverlayBean.Overlay overlay) {
                    View v = parentActivity.showViewContentDialog(R.layout.add_caption_dialog, parentActivity.getString(R.string.str_add), new Runnable() {
                        @Override
                        public void run() {
                            if (editTitle != null) {
                                String strCaption = editTitle.getText().toString();
                                if (strCaption.length() > 0) {
                                    addNewTitleInformation(strCaption, overlay, time);
                                }
                            }
                        }
                    }, parentActivity.getString(R.string.str_cancel));
                    editTitle = (EditText)v.findViewById(R.id.edit_title_name);
                    editTitle.setTextColor(overlay.color);
                    ImageView overlayBackground = (ImageView)v.findViewById(R.id.overlay_background);
                    String filePath = Constant.getApplicationDirectory() + MainApplication.getInstance().getTemplate().strDirectoryID + File.separator + overlay.backgroundImage;;

                    File imgFile = new  File(filePath);
                    if(imgFile.exists()){
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                        overlayBackground.setImageBitmap(myBitmap);
                        overlayBackground.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                }
            });

        }
    }

    /**
     * when video is trimming, it must affected to captions too.
     * @param caption
     */
    private void updateCaptionLayoutForTrimView(ChildTextTimelineLayout caption, boolean flagResize) {
        boolean flagUpdate = false;
        float startTime = caption.getStartTime();
        float endTime = caption.getEndTime();

        if (startTime < trimStart) {
            flagUpdate = true;
            float delta = trimStart - startTime;
            startTime = startTime + delta;
            if (flagResize) {
                endTime = endTime + delta;
                if (endTime >= trimEnd) {
                    endTime = trimEnd;
                }
            }
        }

        if (endTime > trimEnd) {
            flagUpdate = true;
            float delta = endTime - trimEnd;
            endTime = endTime - delta;
            if (flagResize) {
                startTime = startTime - delta;
                if (startTime < trimStart) {
                    startTime = trimStart;
                }
            }

        }

        if (flagUpdate) {
            caption.updateStartEndTime(startTime, endTime);
        }
    }
    public void setTrimLeftRight(int trimStart, int trimEnd) {
        ArrayList<ChildTextTimelineLayout>  timelineTitlesInformation = MainApplication.getTimelineTitlesInformation();

        this.trimStart = trimStart / 1000.f;
        this.trimEnd = (trimEnd) / 1000.f;
//        for (int i = 0; i < timelineTitlesInformation.size(); i ++) {
//            ChildTextTimelineLayout caption = timelineTitlesInformation.get(i);
//            updateCaptionLayoutForTrimView(caption, true);
//        }
    }
}
