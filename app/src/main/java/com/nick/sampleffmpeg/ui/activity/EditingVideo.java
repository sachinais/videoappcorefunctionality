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
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.ui.control.UITouchButton;
import com.nick.sampleffmpeg.ui.view.ChildTextTimelineLayout;
import com.nick.sampleffmpeg.ui.view.OverlayView;
import com.nick.sampleffmpeg.ui.view.StretchVideoView;
import com.nick.sampleffmpeg.ui.view.TitleTimeLayout;
import com.nick.sampleffmpeg.ui.view.WaveformView;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.LogFile;
import com.nick.sampleffmpeg.utils.StringUtils;
import com.nick.sampleffmpeg.utils.audio.soundfile.SoundFile;
import com.nick.sampleffmpeg.utils.ffmpeg.FFMpegUtils;

import java.util.ArrayList;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by baebae on 12/24/15.
 */
public class EditingVideo extends BaseActivity {

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

    @InjectView(R.id.left_sidebar_layout)
    RelativeLayout layoutTimelineLeftSidebar;

    @InjectView(R.id.right_sidebar_layout)
    RelativeLayout layoutTimelineRightSidebar;

    @InjectView(R.id.overlay_layout)
    OverlayView overlayView;

    @InjectView(R.id.video_view_tap_area)
    View video_view_tap_area;

    private int currentVideoSeekPosition = 0;
    private int videoLength = 0;
    private boolean flagPlay = false;

    private Handler mHandler = null;
    private View selectedVideoTimeline = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.editing_video_view);
        ButterKnife.inject(this);

        initializeButtons();

        overlayView.setRecordingView(false);
        LogFile.clearLogText();
        mHandler = new Handler();
        addTitle();
    }

    /**
     * show dialog which require input title information.
     */
    private static EditText editTitle = null;

    private void addTitle() {
        View v = showViewContentDialog(R.layout.add_caption_dialog, getString(R.string.str_set), new Runnable() {
            @Override
            public void run() {
                if (editTitle != null) {
                    String strTitle = editTitle.getText().toString();
                    if (strTitle.length() > 0) {
                        editJobTitle.setText(strTitle);
                        overlayView.setJobTitle(strTitle);
                        initializeVideoView();
                        initializeThumbView();
                    }
                }
            }
        }, getString(R.string.str_cancel));
        editTitle = (EditText)v.findViewById(R.id.edit_title_name);
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
                        EditingVideo.this.finish();
                    }
                });

        UITouchButton.applyEffect(btnEncodeVideo, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        convertVideoToUniqueFormat();
                    }
                });

        UITouchButton.applyEffect(layoutTimelineLeftSidebar, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        scrollViewTimeline.scrollBy(-getPixelFromDensity(Constant.SP_PER_SECOND * Constant.TIMELINE_UNIT_SECOND), 0);
                    }
                });

        UITouchButton.applyEffect(layoutTimelineRightSidebar, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        scrollViewTimeline.scrollBy(getPixelFromDensity(Constant.SP_PER_SECOND * Constant.TIMELINE_UNIT_SECOND), 0);
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
            mHandler.postDelayed(this, 300);
        }
    };

    /**
     * set current video 's seek time.
     * @param time
     */
    public void setCurrentSeekTime(int time) {
        currentVideoSeekPosition = time;

        if (!flagPlay) {
            videoView.seekTo(currentVideoSeekPosition);
            videoView.pause();
        }

        int index = time / 1000 / Constant.TIMELINE_UNIT_SECOND;
        setTimelineVideo(videoThumbsLayout.findViewWithTag(index));

        updateOverlayView(time);
    }

    /**
     * update overlay view during play or once timeline is selected
     */
    public void updateOverlayView(int time) {
//        ArrayList<ChildTextTimelineLayout> titleList = titleThumbsLayout.getTimelineTitlesInformation();
//        ChildTextTimelineLayout showingTitle = null;
//        for (int i = 0; i < titleList.size(); i ++) {
//            ChildTextTimelineLayout title = titleList.get(i);
//            if (title.getStartTime() * 1000 <= currentVideoSeekPosition && currentVideoSeekPosition < title.getEndTime() * 1000) {
//                showingTitle = title;
//                break;
//            }
//        }
//        if (showingTitle == null) {
//            overlayView.setVisibility(View.GONE);
//        } else {
//            overlayView.setVisibility(View.VISIBLE);
//            overlayView.setText(showingTitle.getTitleText());
//        }
    }
    /**
     * Play & Pause video in video view
     */
    private void onPlay() {
        if (!flagPlay) {
            flagPlay = true;
            videoView.start();
            mHandler.postDelayed(updateTimeTask, 300);
        } else {
            flagPlay = false;
            videoView.pause();
            currentVideoSeekPosition = videoView.getCurrentPosition();
            mHandler.removeCallbacks(updateTimeTask);
        }
    }
    /**
     * initialize video view
     * set media file for video view, once it is initialized control layout will be shown below video view
     */
    private void initializeVideoView() {
        videoControlLayout.setVisibility(View.GONE);

        videoView.setVideoPath(Constant.getCameraVideo());
        videoView.requestFocus();
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoLength = videoView.getDuration();
                videoView.seekTo(500);
                txtVideoDuration.setText(getString(R.string.label_total_length) + StringUtils.getMinuteSecondString(videoLength / 1000, false));
                videoControlLayout.setVisibility(View.VISIBLE);
                initializeTimeLineView();
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                flagPlay = false;
                setCurrentSeekTime(500);
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
        bmThumb = ThumbnailUtils.createVideoThumbnail(Constant.getCameraVideo(), MediaStore.Video.Thumbnails.MINI_KIND);

        imgThumbVideo1.setImageBitmap(bmThumb);
        imgThumbVideo2.setImageBitmap(bmTopThumb);
        imgThumbVideo3.setImageBitmap(bmTailThumb);
    }

    private void mergeEncodingVideoWithTopTailVideo() {
        //make ffmpeg command
        String command = "-y ";
        command = command + "-i" + " " + Constant.getTopVideo() +" ";
        command = command + "-i" + " " + Constant.getEncodedVideo() +" ";
        command = command + "-i" + " " + Constant.getTailVideo() +" ";

        command = command + "-c:a aac -strict experimental -threads 5 -preset ultrafast -r 30 -c:v libx264 -map [v] -map [a] -filter_complex";

        String strFilterComplex = "[0:0] [0:1] [1:0] [1:1] [2:0] [2:1] concat=n=3:v=1:a=1 [v] [a]";

        String[] subCommands = command.split(" ");

        String[] commands = new String[subCommands.length + 2];
        for (int i = 0; i < subCommands.length; i ++) {
            commands[i] = subCommands[i];
        }
        commands[subCommands.length] = strFilterComplex;
        commands[subCommands.length + 1] = Constant.getMergedVideo();

        progressDialog.show();
        progressDialog.setMessage(getString(R.string.str_merge_videos));
        FFMpegUtils.execFFmpegBinary(commands, new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                EditingVideo.this.finish();
                showActivity(CompleteActivity.class, null);
            }
        });
    }
    /**
     * start encoding video
     */
    private void startEncodingVideo() {
        //Convert overlay into png
        ArrayList<ChildTextTimelineLayout> titleList = titleThumbsLayout.getTimelineTitlesInformation();
        if (titleList.size() == 0) {
            return;
        }
        for (int i = 0; i < titleList.size(); i ++) {
            ChildTextTimelineLayout title = titleList.get(i);
            FileUtils.createTitleCaptionPNG(title.getTitleText(), Constant.getOverlayDirectory() + i + ".png");
        }

        //make ffmpeg command
        String command = "-y ";

        command = command + "-i" + " " + Constant.getConvertedVideo() +" ";
        for (int i = 0; i < titleList.size(); i ++) {
            command = command + "-i" + " " + Constant.getOverlayDirectory() + i + ".png ";
        }

        command = command + "-c:a aac -strict experimental -threads 5 -preset ultrafast -r 30 -c:v libx264 -filter_complex";

        String strFilterComplex = "[0:v][1:v] overlay=0:570:enable='between(t," + titleList.get(0).getStartTime() + "," + + titleList.get(0).getEndTime() + ")' ";
        for (int i = 1; i < titleList.size(); i ++) {
            ChildTextTimelineLayout title = titleList.get(i);
            strFilterComplex += "[tmp];[tmp][" + Integer.toString(i + 1) + ":v] overlay=0:570:enable='between(t," + title.getStartTime() + "," + + title.getEndTime() + ")' ";
        }

        String[] subCommands = command.split(" ");

        String[] commands = new String[subCommands.length + 2];
        for (int i = 0; i < subCommands.length; i ++) {
            commands[i] = subCommands[i];
        }
        commands[subCommands.length] = strFilterComplex;
        commands[subCommands.length + 1] = Constant.getEncodedVideo();

        progressDialog.show();
        progressDialog.setMessage(getString(R.string.str_encoding_video));

        FFMpegUtils.execFFmpegBinary(commands, new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();

                mergeEncodingVideoWithTopTailVideo();
            }
        });
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
    private class InitializeTimelineTask extends AsyncTask<Boolean, Boolean, Boolean> {
        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Initializing timeline...");
            progressDialog.show();
            super.onPreExecute();
        }

        /**
         * Extract image from video, and showing them on video timeline
         */
        private void initializeVideoTimeLine() {
            boolean flagInitialize = false;
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            try {
                retriever.setDataSource(Constant.getCameraVideo());
                flagInitialize = true;
            } catch (Exception ex) {
            }

            if (flagInitialize) {
                int sec = videoLength / 1000;
                for (int i = 0; i < sec; i += Constant.TIMELINE_UNIT_SECOND) {
                    final Integer tagObj = new Integer(i);
                    final Bitmap bitmap = retriever.getFrameAtTime(i * 1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final View thumbImageLayout = getLayoutInflater().inflate(R.layout.video_timeline_thumb_layout, null);
                            ImageView imageView = (ImageView)thumbImageLayout.findViewById(R.id.img_thumb);
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
            try {
                retriever.release();
            } catch (Exception ex) {
                // Ignore failures while cleaning up.
            }
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
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progressDialog.dismiss();
        }
    }
    /**
     * initialize Timeline view extract thumb image from video, add them into video timeline
     */
    private void initializeTimeLineView() {
        titleThumbsLayout.setVideoLength(videoLength);
        videoThumbsLayout.removeAllViews();
        titleThumbsLayout.setActivity(this);
        titleThumbsLayout.removeChildTitleLayouts();
        new InitializeTimelineTask().execute(true);
    }



    /**
     * Convert recording video into unique video format 1280 * 720 720p format
     */
    private void convertVideoToUniqueFormat() {

        String commands = "-y -threads 5 -i src.mp4 -crf 30 -preset ultrafast -ar 44100 -c:a aac -strict experimental -s 1280x720 -r 30 -force_key_frames expr:gte(t,n_forced*1) -c:v libx264 dst.mp4";

        String srcVideoFilePath = Constant.getCameraVideo();
        String dstVideoFilePath = Constant.getConvertedVideo();

        commands = commands.replace("src.mp4", srcVideoFilePath);
        commands = commands.replace("dst.mp4", dstVideoFilePath);

        String[] command = commands.split(" ");

        progressDialog.show();
        progressDialog.setMessage(getString(R.string.str_convert_camera_unique_format));
        FFMpegUtils.execFFmpegBinary(command, new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();

                if (!FileUtils.isExistFile(Constant.getTailVideo()) || !FileUtils.isExistFile(Constant.getTopVideo())) {
                    convertTopTailVideoToUniqueFormat(true);
                } else {
                    startEncodingVideo();
                }
            }
        });
    }

    /**
     * Convert recording video into unique video format 1280 * 720 720p format
     */
    private void convertTopTailVideoToUniqueFormat(final boolean flagTop) {

        String commands = "-y -threads 5 -i src.mp4 -crf 30 -preset ultrafast -ar 44100 -c:a aac -strict experimental -s 1280x720 -r 30 -force_key_frames expr:gte(t,n_forced*1) -c:v libx264 dst.mp4";
        String srcVideoFilePath = "";
        String dstVideoFilePath = "";
        progressDialog.show();

        if (flagTop) {
            srcVideoFilePath = Constant.getAssetTopVideo();
            dstVideoFilePath = Constant.getTopVideo();
            progressDialog.setMessage(getString(R.string.str_convert_asset_top_video));
        } else {
            srcVideoFilePath = Constant.getAssetTailVideo();
            dstVideoFilePath = Constant.getTailVideo();
            progressDialog.setMessage(getString(R.string.str_convert_asset_tail_video));
        }

        commands = commands.replace("src.mp4", srcVideoFilePath);
        commands = commands.replace("dst.mp4", dstVideoFilePath);

        String[] command = commands.split(" ");

        FFMpegUtils.execFFmpegBinary(command, new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
                if (flagTop) {
                    convertTopTailVideoToUniqueFormat(false);
                } else {
                    startEncodingVideo();
                }
            }
        });
    }

    public int getCurrentSeekPosition() {
        return currentVideoSeekPosition;
    }
}
