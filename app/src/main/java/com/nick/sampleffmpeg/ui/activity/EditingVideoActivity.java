package com.nick.sampleffmpeg.ui.activity;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.OverlayBean;
import com.nick.sampleffmpeg.bean.VideoOverlay;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;
import com.nick.sampleffmpeg.ui.control.UITouchButton;
import com.nick.sampleffmpeg.ui.view.ChildTextTimelineLayout;
import com.nick.sampleffmpeg.ui.view.OverlayView;
import com.nick.sampleffmpeg.ui.view.StretchVideoView;
import com.nick.sampleffmpeg.ui.view.TitleTimeLayout;
import com.nick.sampleffmpeg.ui.view.WaveformView;
import com.nick.sampleffmpeg.utils.LogFile;
import com.nick.sampleffmpeg.utils.StringUtils;
import com.nick.sampleffmpeg.utils.VideoUtils;
import com.nick.sampleffmpeg.utils.audio.soundfile.SoundFile;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by baebae on 12/24/15.
 */
public class EditingVideoActivity extends BaseActivity {

    @InjectView(R.id.txt_job_title)
    EditText editJobTitle;

    @InjectView(R.id.video_view)
    StretchVideoView videoView;

    @InjectView(R.id.video_control_layout)
    RelativeLayout videoControlLayout;

    @InjectView(R.id.btn_back)
    ImageView btnBack;

    @InjectView(R.id.btn_encode_video)
    ImageView btnEncodeVideo;

    @InjectView(R.id.btn_play)
    ImageView btnPlay;

    @InjectView(R.id.btn_next)
    ImageView btnNext;

    @InjectView(R.id.btn_previous)
    ImageView btnPrevious;

    @InjectView(R.id.img_thumb_video1)
    ImageView imgThumbVideo1;

    @InjectView(R.id.img_thumb_video2)
    ImageView imgThumbVideo2;

    @InjectView(R.id.img_thumb_video3)
    ImageView imgThumbVideo3;

    @InjectView(R.id.img_thumb_video4)
    ImageView imgThumbVideo4;

    @InjectView(R.id.txt_video_duration)
    TextView txtVideoDuration;

    @InjectView(R.id.timeline_video_thumbs_layout)
    ViewGroup videoThumbsLayout;

    @InjectView(R.id.timeline_title_thumbs_layout)
    TitleTimeLayout titleThumbsLayout;

    @InjectView(R.id.wavform_view)
    WaveformView wavFormView;

    @InjectView(R.id.scrollview_timeline)
    HorizontalScrollView scrollViewTimeline;

    @InjectView(R.id.trim_left)
    ImageView trimLeftLayout;

    @InjectView(R.id.redo_trim_left)
    ImageView redoTrimLeftLayout;

    @InjectView(R.id.trim_right)
    ImageView trimRightLayout;

    @InjectView(R.id.redo_trim_right)
    ImageView redoTrimRightLayout;

    @InjectView(R.id.overlay_layout)
    OverlayView overlayView;

    @InjectView(R.id.video_view_tap_area)
    View video_view_tap_area;

    @InjectView(R.id.video_indicator)
    View video_indicator;

    @InjectView(R.id.video_trim_left_view)
    View video_trim_left;

    @InjectView(R.id.video_trim_right_view)
    View video_trim_right;

    private int currentVideoSeekPosition = 0;
    private double trimLeft = 3.0;
    private double trimRight = 1.0;
    private int videoLength = 0;
    private boolean flagPlay = false;
    private boolean flagTimelineInitialized = false;

    private int defaultVideoInitialTime = 50;
    private int videoIndicatorWidth = 10;

    private Handler mHandler = null;
    private View selectedVideoTimeline = null;

    private boolean flagFromBackground = false;
    private SharedPreferenceWriter sharedPreferenceWriter = null;

    private static String strJobTitle = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editing_video_view);
        ButterKnife.inject(this);

        sharedPreferenceWriter = SharedPreferenceWriter.getInstance(this);
        initializeButtons();

        overlayView.setRecordingView(false, false);
        LogFile.clearLogText();
        mHandler = new Handler();
        if (savedInstanceState == null) {
            flagFromBackground = false;
            addTitle();
        } else {
            flagFromBackground = true;
            initializeVideoView();
            initializeThumbView();
            editJobTitle.setText(strJobTitle);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    /**
     * show dialog which require input title information.
     */
    private static EditText editTitle = null;

    private void addTitle() {
        View v = showViewContentDialog(R.layout.add_title_dialog, getString(R.string.str_set), new Runnable() {
            @Override
            public void run() {
                if (editTitle != null) {
                    strJobTitle = editTitle.getText().toString();
                    editJobTitle.setText(strJobTitle);
                    initializeVideoView();
                    initializeThumbView();
                }
            }
        });
        editTitle = (EditText)v.findViewById(R.id.edit_title_name);
    }

    private void updateVideoTrimLayout() {
        double leftWidth = (trimLeft * Constant.SP_PER_SECOND * getDisplayMetric().scaledDensity);
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams((int)leftWidth, FrameLayout.LayoutParams.MATCH_PARENT);
        param.setMargins((int)0, 0, 0, 0);
        video_trim_left.setLayoutParams(param);

        double rightWidth = (trimRight * Constant.SP_PER_SECOND * getDisplayMetric().scaledDensity);
        param = new FrameLayout.LayoutParams((int)rightWidth, FrameLayout.LayoutParams.MATCH_PARENT);
        double marginLeft = (((double)videoLength / 1000.0 - trimRight) * Constant.SP_PER_SECOND * getDisplayMetric().scaledDensity);
        param.setMargins((int)marginLeft, 0, 0, 0);
        video_trim_right.setLayoutParams(param);

    }
    private void initializeButtons() {
        UITouchButton.applyEffect(btnNext, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        if (!flagPlay) {
                            currentVideoSeekPosition += Constant.TIMELINE_UNIT_SECOND * 1000;
                            if (currentVideoSeekPosition >= videoLength) {
                                currentVideoSeekPosition = videoLength;
                            }
                            setCurrentSeekTime(currentVideoSeekPosition);
                        }
                    }
                });

        UITouchButton.applyEffect(btnPlay, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        onPlay();
                    }
                });

        UITouchButton.applyEffect(video_view_tap_area, UITouchButton.EFFECT_ALPHA, 0,
                20, new Runnable() {
                    @Override
                    public void run() {
                        onPlay();
                    }
                });

        UITouchButton.applyEffect(btnPrevious, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        if (!flagPlay) {
                            currentVideoSeekPosition -= Constant.TIMELINE_UNIT_SECOND * 1000;
                            if (currentVideoSeekPosition < 0) {
                                currentVideoSeekPosition = 0;
                            }
                            setCurrentSeekTime(currentVideoSeekPosition);
                        }
                    }
                });

        UITouchButton.applyEffect(btnBack, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        EditingVideoActivity.this.finish();
                    }
                });

        UITouchButton.applyEffect(btnEncodeVideo, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        convertOverlaysPNG();
                    }
                });

        UITouchButton.applyEffect(trimLeftLayout, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        trimLeft ++;
                        updateVideoTrimLayout();
                    }
                });

        UITouchButton.applyEffect(trimRightLayout, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        trimRight ++;
                        updateVideoTrimLayout();
                    }
                });

        UITouchButton.applyEffect(redoTrimLeftLayout, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        if (trimLeft > 0) {
                            trimLeft --;
                            updateVideoTrimLayout();
                        }
                    }
                });

        UITouchButton.applyEffect(redoTrimRightLayout, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        if (trimRight > 0) {
                            trimRight --;
                            updateVideoTrimLayout();
                        }
                    }
                });

        scrollViewTimeline.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    titleThumbsLayout.setAlpha(1);
                }
                v.onTouchEvent(event);
                return true;
            }
        });
    }

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            setCurrentSeekTime(videoView.getCurrentPosition());
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     * set current video 's seek time.
     * @param time
     */
    public void setCurrentSeekTime(double time) {
        currentVideoSeekPosition = (int)time;

        if (!flagPlay) {
            videoView.seekTo(currentVideoSeekPosition);
            videoView.pause();
        }
        double width = (videoIndicatorWidth * getDisplayMetric().scaledDensity);
        double marginLeft = (double)time / 1000.0 * Constant.SP_PER_SECOND * (getDisplayMetric().scaledDensity) - width;
        FrameLayout.LayoutParams param = new FrameLayout.LayoutParams((int)width, FrameLayout.LayoutParams.MATCH_PARENT);
        param.setMargins((int)marginLeft, 0, 0, 0);
        video_indicator.setLayoutParams(param);

        int index = (int)(time / 1000 / Constant.TIMELINE_UNIT_SECOND);
        setTimelineVideo(videoThumbsLayout.findViewWithTag(index));

        updateOverlayView((float)time / 1000.f);
    }

    /**
     * update overlay view during play or once timeline is selected
     */
    public void updateOverlayView(double time) {
        overlayView.setCurrentVideoTime(time);
    }
    /**
     * Play & Pause video in video view
     */
    private void onPlay() {
        if (!flagPlay) {
            flagPlay = true;
            videoView.start();
            mHandler.postDelayed(updateTimeTask, 100);
            btnPlay.setImageDrawable(getResources().getDrawable(R.drawable.btn_pause));
        } else {
            flagPlay = false;
            videoView.pause();
            currentVideoSeekPosition = videoView.getCurrentPosition();
            mHandler.removeCallbacks(updateTimeTask);
            btnPlay.setImageDrawable(getResources().getDrawable(R.drawable.btn_play));
        }
    }
    /**
     * initialize video view
     * set media file for video view, once it is initialized control layout will be shown below video view
     */
    private void initializeVideoView() {
        videoControlLayout.setVisibility(View.GONE);

        videoView.setVideoPath(Constant.getSourceVideo());
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoLength = videoView.getDuration();
                videoView.seekTo(defaultVideoInitialTime);
                txtVideoDuration.setText(getString(R.string.label_total_length) + StringUtils.getMinuteSecondString(videoLength / 1000, false));
                videoControlLayout.setVisibility(View.VISIBLE);
                if (!flagTimelineInitialized) {
                    initializeTimeLineView();
                }
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                flagPlay = false;
                btnPlay.setImageDrawable(getResources().getDrawable(R.drawable.btn_play));
                setCurrentSeekTime(defaultVideoInitialTime);
            }
        });
    }

    /**
     * Initialize thumb views on left side ,right side of app
     */
    private void initializeThumbView() {
        Bitmap bmTailThumb;
        bmTailThumb = ThumbnailUtils.createVideoThumbnail(Constant.getAssetTailVideo(), MediaStore.Video.Thumbnails.MINI_KIND);

        Bitmap bmTopThumb;
        bmTopThumb = ThumbnailUtils.createVideoThumbnail(Constant.getAssetTopVideo(), MediaStore.Video.Thumbnails.MINI_KIND);

        Bitmap bmThumb;
        bmThumb = ThumbnailUtils.createVideoThumbnail(Constant.getSourceVideo(), MediaStore.Video.Thumbnails.MINI_KIND);

        imgThumbVideo1.setImageBitmap(bmThumb);
        imgThumbVideo2.setImageBitmap(bmTopThumb);
        imgThumbVideo3.setImageBitmap(bmTailThumb);
    }

    /**
     * Convert overlay into png && save video overlay information..
     */
    private void convertOverlaysPNG() {
        ArrayList<VideoOverlay> videoOverlayInformation = MainApplication.getInstance().getVideoOverlayInformation();
        videoOverlayInformation.clear();

        int videoWidth = VideoUtils.getVideoWidth(Constant.getSourceVideo());
        int videoHeight = VideoUtils.getVideoHeight(Constant.getSourceVideo());

        ArrayList<ChildTextTimelineLayout> titleList = MainApplication.getTimelineTitlesInformation();
         OverlayBean overlayBean = MainApplication.getInstance().getTemplate();

        //convert brand overlay into png
        if (overlayBean.brandLogo != null && overlayBean.brandLogo.backgroundImage.length() > 0) {
            OverlayBean.Overlay brandOverlay = overlayBean.brandLogo;
            String fileName = Constant.getOverlayDirectory() + "0.png";
            overlayView.convertOverlayToPNG("", brandOverlay, videoWidth, videoHeight, fileName);
            int x = (int)(videoWidth * (brandOverlay.x / 100.f));
            int y = (int)(videoHeight * (brandOverlay.y / 100.f));
            VideoOverlay info = new VideoOverlay(0, videoLength, x, y, fileName);
            videoOverlayInformation.add(info);
        }

        //convert caption overlay into image.
        for (int i = 0; i < titleList.size(); i ++) {
            ChildTextTimelineLayout title = titleList.get(i);
            String fileName =  Constant.getOverlayDirectory() + (i + 1) + ".png";
            overlayView.convertOverlayToPNG(title.getTitleText(), title.getCaptionOverlay(), videoWidth, videoHeight, fileName);

            int x = (int)(videoWidth * (title.getCaptionOverlay().x / 100.f));
            int y = (int)(videoHeight * (title.getCaptionOverlay().y / 100.f));
            VideoOverlay info = new VideoOverlay(title.getStartTime(), title.getEndTime(), x, y, fileName);
            videoOverlayInformation.add(info);
        }

        showActivity(UploadingVideoScreen.class, editJobTitle.getText().toString());
        return;

    }

    /**
     * highlight video image in timeline area.
     * @param view
     */
    private void setTimelineVideo(View view) {
        if (view == null)
            return;
        if (selectedVideoTimeline != null) {
            selectedVideoTimeline.setAlpha(1.f);
        }
        selectedVideoTimeline = view;
        view.setAlpha(0.5f);
    }
    /**
     * Intialize timeline , extract images from video on every 1 secs, analyze audio, and showing into the wavform view.
     */
    private boolean flagProgressDialogIsRunning = false;
    private class InitializeTimelineTask extends AsyncTask<Boolean, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            try {
                if (!flagProgressDialogIsRunning) {
                    progressDialog.setMessage("Preparing video...");
                    progressDialog.show();
                    flagProgressDialogIsRunning = true;
                }

            } catch (Exception e) {

            }
            super.onPreExecute();
        }

        /**
         * Extract image from video, and showing them on video timeline
         */
        private void initializeVideoTimeLine() {
            boolean flagInitialize = false;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(Constant.getSourceVideo());
                flagInitialize = true;
            } catch (Exception ex) {
            }

            if (flagInitialize) {
                int sec = videoLength / 1000;
                for (int i = 0; i < sec; i += Constant.TIMELINE_UNIT_SECOND) {
                    if (isCancelled()) {
                        videoThumbsLayout.removeAllViews();
                        return;
                    }
                    final Integer tagObj = new Integer(i);
                    final Bitmap bitmap = retriever.getFrameAtTime(i * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    if (bitmap != null) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                final View thumbImageLayout = getLayoutInflater().inflate(R.layout.video_timeline_thumb_layout, null);
                                ImageView imageView = (ImageView)thumbImageLayout.findViewById(R.id.img_thumb);

                                if (tagObj == (int)videoLength / 1000 - Constant.TIMELINE_UNIT_SECOND) {
                                    double offset = (double)videoLength /1000 - (double)tagObj;
                                    int width = (int)(offset * 60 * getDisplayMetric().scaledDensity / Constant.TIMELINE_UNIT_SECOND);
                                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
                                    imageView.setLayoutParams(param);
                                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                }
                                if (bitmap != null && imageView != null) {
                                    imageView.setImageBitmap(bitmap);
                                    thumbImageLayout.setTag(tagObj / Constant.TIMELINE_UNIT_SECOND);
                                    videoThumbsLayout.addView(thumbImageLayout);

                                    /**
                                     * once image is pressed, video seek position will be changed.
                                     */
                                    UITouchButton.applyEffect(thumbImageLayout, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                                            Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                                                @Override
                                                public void run() {
                                                    currentVideoSeekPosition = (Integer)thumbImageLayout.getTag() * 1000 * Constant.TIMELINE_UNIT_SECOND;
                                                    setCurrentSeekTime(currentVideoSeekPosition);
                                                }
                                            });
                                }

                                if (tagObj == 0) {
                                    setTimelineVideo(thumbImageLayout);
                                }
                            }
                        });
                    }
                }
            }
            try {
                retriever.release();
            } catch (Exception ex) {
                // Ignore failures while cleaning up.
            }
            flagTimelineInitialized = true;
        }

        /**
         * Extract audio samples from wav file, and analyze, drawing them into wavform view;
         */
        int[] frameGains = null;
        int sampleRate = 0;
        int samplePerFrame = 0;
        int frameCount = 0;
        private void initializeAudioTimeline() {
            try {
                SoundFile soundFile = SoundFile.create(Constant.getConvertedAudio(), null);
                frameGains = new int[soundFile.getNumFrames()];
                for (int i = 0; i < frameGains.length; i ++) {
                    frameGains[i] = soundFile.getFrameGains()[i];
                }

                sampleRate = soundFile.getSampleRate();
                samplePerFrame = soundFile.getSamplesPerFrame();
                frameCount = soundFile.getNumFrames();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        wavFormView.setAudioInformation(sampleRate, samplePerFrame, frameCount, frameGains);
                        wavFormView.invalidate();

                    }
                });

                soundFile = null;
            }catch (Exception e) {

            }
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {
            initializeVideoTimeLine();
            initializeAudioTimeline();

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    titleThumbsLayout.setActivity(EditingVideoActivity.this);

                    ArrayList<ChildTextTimelineLayout> timelineInfo = MainApplication.getTimelineTitlesInformation();
                    if (timelineInfo.size() > 0 && flagFromBackground == true) {
                        cachedInfo = new ArrayList<ChildTextTimelineLayout>(timelineInfo.size());
                        cachedInfo.addAll(timelineInfo);
                    }

                    titleThumbsLayout.removeChildTitleLayouts();
                    if (flagFromBackground) {
                        restoreTimelineLayout();
                    } else {
                        addDefaultOverlays();
                    }
                    updateVideoTrimLayout();
                }
            });

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try {
                progressDialog.dismiss();
                flagProgressDialogIsRunning = false;
                mTask = null;
            } catch (Exception e) {

            }
        }
    }

    private static ArrayList<ChildTextTimelineLayout> cachedInfo = null;
    private void restoreTimelineLayout() {
        titleThumbsLayout.removeChildTitleLayouts();
        if (cachedInfo != null) {
            for (int i = 0 ; i <cachedInfo.size(); i ++) {
                ChildTextTimelineLayout cachedItem = cachedInfo.get(i);
                titleThumbsLayout.addNewTitleInformation(cachedItem.getTitleText(), cachedItem.getStartTime(), cachedItem.getEndTime(), cachedItem.getCaptionOverlay(), cachedItem.isRemovable());
            }
        }
        updateOverlayView(0);
    }
    /**
     *
     */
    private void addDefaultOverlays () {
        //add default captions into timeline view (contact, name overlay)

        OverlayBean template = MainApplication.getInstance().getTemplate();
        OverlayBean.Overlay nameOverlay = template.name;
        if (nameOverlay != null) {
            titleThumbsLayout.addNewTitleInformation(nameOverlay.defaultText, 0, Constant.TIMELINE_UNIT_SECOND, nameOverlay, false);
        }

        OverlayBean.Overlay contactOverlay = template.contact;
        if (contactOverlay != null) {
            double lastSec = (double)videoLength / 1000;
            titleThumbsLayout.addNewTitleInformation(contactOverlay.defaultText, lastSec - Constant.TIMELINE_UNIT_SECOND, lastSec, contactOverlay, false);
        }
        updateOverlayView(0);
    }
    /**
     * initialize Timeline view extract thumb image from video, add them into video timeline
     */
    private InitializeTimelineTask mTask = null;
    private void initializeTimeLineView() {
        videoThumbsLayout.removeAllViews();
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
        mTask = new InitializeTimelineTask();
        mTask.execute(true);
    }

    public int getCurrentSeekPosition() {
        return currentVideoSeekPosition;
    }
}
