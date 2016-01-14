package com.nick.sampleffmpeg.ui.view;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.RelativeLayout;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.OverlayBean;
import com.nick.sampleffmpeg.ui.activity.EditingVideoActivity;
import com.nick.sampleffmpeg.ui.adapter.CaptionPreviewAdapter;

import java.util.ArrayList;

/**
 * Created by baebae on 12/25/15.
 */
public class TitleTimeLayout extends RelativeLayout  implements View.OnTouchListener {

    private ArrayList<ChildTextTimelineLayout> timelineTitlesInformation = new ArrayList<>();

    private EditingVideoActivity parentActivity = null;
    private ChildTextTimelineLayout selectedItem = null;
    private boolean flagResizeLeft = false;
    private boolean flagResizeRight = false;
    private boolean flagMoving = false;

    private double movingOffset = 0;

    private boolean flagDragging;
    private long dragStartTime;
    private float startDragX;
    private double lastDragX;

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

    public ArrayList<ChildTextTimelineLayout> getTimelineTitlesInformation() {
        return  timelineTitlesInformation;
    }

    public void removeChildTitleLayouts() {
        for (int i = 0; i < timelineTitlesInformation.size(); i ++) {
            removeView(timelineTitlesInformation.get(i));
        }
        timelineTitlesInformation.clear();
    }

    private boolean checkTitleAlreadyExistInCurrentTimeLine(int currentVideoSeekPosition) {
        boolean ret = false;

        for (int i = 0; i < timelineTitlesInformation.size(); i ++) {
            double startTime = timelineTitlesInformation.get(i).getStartTime();
            double endTime = timelineTitlesInformation.get(i).getEndTime();
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
    private void addNewTitleInformation(String title, OverlayBean.Overlay overlay) {
        int currentVideoSeekPosition = parentActivity.getCurrentSeekPosition();
        boolean flagAlreadyHaveTitle = checkTitleAlreadyExistInCurrentTimeLine(currentVideoSeekPosition);
        if (flagAlreadyHaveTitle) {
            parentActivity.showAlert(R.string.str_alert_title_information, R.string.str_title_already_exist, parentActivity.getString(R.string.str_alert_okay));
        } else {
            int startTime = currentVideoSeekPosition / 1000;
            int endTime = startTime + Constant.TIMELINE_UNIT_SECOND;

            addNewTitleInformation(title, startTime, endTime, overlay, true);
        }
    }

    public void addNewTitleInformation(String title, double startTime, double endTime, OverlayBean.Overlay overlay, boolean flagRemovable) {

        ChildTextTimelineLayout titleLayout = (ChildTextTimelineLayout)parentActivity.getLayoutInflater().inflate(R.layout.title_timeline_layout, null);
        titleLayout.setParentWidth(this.getWidth());
        titleLayout.setDisplayMetrics(parentActivity.getDisplayMetric());
        titleLayout.setInformation(startTime, endTime, title, System.currentTimeMillis(), overlay, flagRemovable);

        addView(titleLayout);
        timelineTitlesInformation.add(titleLayout);
        parentActivity.updateOverlayView(startTime);


        showHintTextView();
    }

    /**
     * remove child title layout from timeline
     * @param layout
     */
    private void removeChildLayout(ChildTextTimelineLayout layout) {
        timelineTitlesInformation.remove(layout);
        removeView(layout);
        selectedItem = null;
    }
    /**
     * show/hide hint text on title timeline are (Tap to add title...)
     */
    private void showHintTextView() {
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
    private ChildTextTimelineLayout getChildFromPosition(double position) {
        flagMoving = flagResizeLeft = flagResizeRight = false;
        for (int i = 0; i < timelineTitlesInformation.size(); i ++) {
            ChildTextTimelineLayout layout = timelineTitlesInformation.get(i);
            if (position > layout.getLayoutLeft() && position < layout.getLayoutRight()) {
                double leftMovingArea = layout.getLayoutLeft() + (layout.getLayoutRight() - layout.getLeft()) / 4.f;
                double rightMovingArea = layout.getLayoutLeft() + (layout.getLayoutRight() - layout.getLeft()) / 4.f * 3;
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
    private boolean checkConflictTitleArea(double position) {
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

    @Override
    public boolean onTouch(View view, MotionEvent me) {
        // Only capture drag events if we start
        // Touch begin and get title from selected position
        if (me.getAction() == MotionEvent.ACTION_DOWN) {
            selectedItem = null;
            lastDragX = startDragX = me.getX();
            selectedItem = getChildFromPosition(startDragX);
            dragStartTime = SystemClock.elapsedRealtime();
            if (selectedItem != null) {
                flagDragging = true;
                getParent().requestDisallowInterceptTouchEvent(true);
            } else {
                setAlpha(0.5f);
            }
            return true;
        }
        //Touch end event
        else if (me.getAction() == MotionEvent.ACTION_UP) {
            getParent().requestDisallowInterceptTouchEvent(false);
            flagDragging = false;

            //if touch start & end in short time, press event will happen.
            if (((SystemClock.elapsedRealtime() - dragStartTime) < SINGLE_TAP_MAX_TIME)) {
                int seekVideoTime = (int)((me.getX() / Constant.SP_PER_SECOND / parentActivity.getDisplayMetric().scaledDensity) * 1000);
                parentActivity.setCurrentSeekTime(seekVideoTime);

                /**
                 * if item is already selected on touch down event, it will show alert to delete current title.
                 * if no itme is selected, add dialog will show
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
                    addNewTitleToTimeline();
                }
            }
            setAlpha(1.f);
            return true;
        } else if (me.getAction() == MotionEvent.ACTION_MOVE) {
            double x = me.getX();
            if (Math.abs(x - lastDragX) > 5) {
                if (flagDragging && (flagResizeLeft || flagResizeRight || flagMoving) && !checkConflictTitleArea(x)) {
                    if (flagResizeLeft) {
                        selectedItem.setLayoutLeft(me.getX());
                    } else if (flagResizeRight) {
                        selectedItem.setLayoutRight(me.getX());
                    } else if (flagMoving) {
                        selectedItem.moveLayout(me.getX() + movingOffset);
                    }
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
        View v = parentActivity.showViewContentDialog(R.layout.select_caption_dialog, parentActivity.getString(R.string.str_select), new Runnable() {
            @Override
            public void run() {
                int index = captionPreviewAdapter.getSelectedItem();
                OverlayBean.Overlay overlay = MainApplication.getInstance().getTemplate().captions.get(index);
                callback.onCaptionThemeSelected(overlay);
            }
        }, parentActivity.getString(R.string.str_cancel));
        captionGridView = (GridView)v.findViewById(R.id.gridView);
        captionGridView.setAdapter(captionPreviewAdapter);
        captionGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                final OverlayBean overlayBean = MainApplication.getInstance().getTemplate();

                for (int i = 0; i < parent.getChildCount(); i ++) {
                    View view = parent.getChildAt(i);
                    view.findViewById(R.id.selected).setVisibility(GONE);
                }
                View view = parent.getChildAt(position);
                view.findViewById(R.id.selected).setVisibility(VISIBLE);
                captionPreviewAdapter.setSelectedItem(position);
            }
        });
    }
    /**
     * show dialog which require input title information.
     */
    private static EditText editTitle = null;

    private void addNewTitleToTimeline() {

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
                                    addNewTitleInformation(strCaption, overlay);
                                }
                            }
                        }
                    }, parentActivity.getString(R.string.str_cancel));
                    editTitle = (EditText)v.findViewById(R.id.edit_title_name);
                    editTitle.setTextColor(overlay.color);
                    v.setBackgroundColor(overlay.backgroundColor);
                }
            });

        }

    }
}
