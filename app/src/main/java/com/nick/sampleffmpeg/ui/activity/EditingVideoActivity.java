package com.nick.sampleffmpeg.ui.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.bean.OverlayBean;
import com.nick.sampleffmpeg.bean.ProvinceBean;
import com.nick.sampleffmpeg.bean.VideoOverlay;
import com.nick.sampleffmpeg.encoding.VideoEncoding;
import com.nick.sampleffmpeg.network.CheckNetworkConnection;
import com.nick.sampleffmpeg.network.CustomDialogs;
import com.nick.sampleffmpeg.network.RequestBean;
import com.nick.sampleffmpeg.network.RequestHandler;
import com.nick.sampleffmpeg.network.RequestListner;
import com.nick.sampleffmpeg.sharedpreference.SPreferenceKey;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;
import com.nick.sampleffmpeg.ui.control.UITouchButton;
import com.nick.sampleffmpeg.ui.view.ChildTextTimelineLayout;
import com.nick.sampleffmpeg.ui.view.OverlayView;
import com.nick.sampleffmpeg.ui.view.StretchVideoView;
import com.nick.sampleffmpeg.ui.view.TitleTimeLayout;
import com.nick.sampleffmpeg.ui.view.WaveformView;
import com.nick.sampleffmpeg.utils.BitmapUtils;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.GlideCircleTransform;
import com.nick.sampleffmpeg.utils.LogFile;
import com.nick.sampleffmpeg.utils.StringUtils;
import com.nick.sampleffmpeg.utils.TailDownloader;
import com.nick.sampleffmpeg.utils.TopDownloader;
import com.nick.sampleffmpeg.utils.VideoUtils;
import com.nick.sampleffmpeg.utils.audio.soundfile.SoundFile;
import com.nick.sampleffmpeg.utils.ffmpeg.FFMpegUtils;
import com.nick.sampleffmpeg.utils.youtube.FileDownloader;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by baebae on 12/24/15.
 */
public class EditingVideoActivity extends BaseActivity {

    @InjectView(R.id.txt_job_title)
    TextView editJobTitle;

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

    @InjectView(R.id.trim_left)
    ImageView btnTrimLeft;

    @InjectView(R.id.trim_right)
    ImageView btnTrimRight;

    @InjectView(R.id.layout_job_title)
    LinearLayout layout_job_title;

    private int currentVideoSeekPosition = 0;
    private boolean flagPlay = false;
    private boolean flagTimelineInitialized = false;

    private final int defaultVideoInitialTime = 50;
    private final int videoIndicatorWidth = 20;
    private final int trimImageViewWidth = 20;

    private Handler mHandler = null;
    private View selectedVideoTimeline = null;

    private boolean flagFromBackground = false;
    private SharedPreferenceWriter sharedPreferenceWriter = null;

    private static String strJobTitle = "";
    private ArrayList<ProvinceBean> options1Items = new ArrayList<ProvinceBean>();

    private String thumbNailUrl = "";

    private boolean flagTopVideoDownloaded = true;
    private boolean flagTailVideoDownloaded = true;

    private boolean flagTopVideoConverted = true;
    private boolean flagTailVideoConverted = true;
    private String topImageThumbnailUrl , tailImageThumbUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        registerReceiver(finishActivity, new IntentFilter("Finish_Activity"));
        setContentView(R.layout.editing_video_view);
        ButterKnife.inject(this);

        sharedPreferenceWriter = SharedPreferenceWriter.getInstance(this);
        initializeButtons();

        overlayView.setRecordingView(false, false);
        LogFile.clearLogText();
        mHandler = new Handler();
        if (savedInstanceState == null) {
            flagTimelineInitialized = false;
            flagFromBackground = false;
            if (!Constant.flagDebug) {
                FileUtils.DeleteFolder(Constant.getUniqueFormatDirectory());
            }
            addTitle();
            initializeVideoView();
            initializeThumbView();
            Constant.setDownloadTailVideo("");
            Constant.setDownloadTailVideo("");
        } else {
            flagFromBackground = true;
            initializeVideoView();
            initializeThumbView();
            editJobTitle.setText(strJobTitle);
        }

        findViewById(R.id.rl_Thumb).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(thumbNailUrl!=null && thumbNailUrl.length()>0){
                    Intent intent = new Intent(EditingVideoActivity.this, FullViewThumbNail.class);
                    intent.putExtra("Url",thumbNailUrl );
                    startActivity(intent);
                }else{

                    showAlert(R.string.str_alert_title_information, "Please wait until the thumbnail has downloaded.", "Ok");

                }

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(finishActivity);
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

                }
            }
        });
        editTitle = (EditText) v.findViewById(R.id.edit_title_name);
    }

    private void updateTimelineAfterTrim() {
        int trimStart = MainApplication.getInstance().getVideoStart();
        int trimEnd = MainApplication.getInstance().getVideoEnd();
        titleThumbsLayout.setTrimLeftRight(trimStart, trimEnd);
        if (currentVideoSeekPosition < trimStart) {
            setCurrentSeekTime(trimStart);
        }
        if (currentVideoSeekPosition > trimEnd) {
            setCurrentSeekTime(trimEnd);
        }
        updateVideoDuration();
    }

    private void convertTopVideoVideoFormat() {
        findViewById(R.id.pb_Top).setVisibility(View.GONE);
        findViewById(R.id.pb_Tail).setVisibility(View.GONE);

        findViewById(R.id.pb_Top_Title).setVisibility(View.VISIBLE);
        findViewById(R.id.pb_Tail_Title).setVisibility(View.VISIBLE);
        if (flagTopVideoDownloaded && flagTailVideoDownloaded) {
            if (mTask != null) {
                VideoEncoding.convertTopTailVideoToUniqueFormat(Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT, true, new Runnable() {
                    @Override
                    public void run() {
                        if (mTask != null) {
                            VideoEncoding.convertTopTailVideoToUniqueFormat(Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT, false, new Runnable() {
                                @Override
                                public void run() {
                                    flagTopVideoConverted = true;
                                    flagTailVideoConverted = true;
                                    findViewById(R.id.pb_Top_Title).setVisibility(View.GONE);
                                    findViewById(R.id.pb_Tail_Title).setVisibility(View.GONE);
                                    initializeThumbView();
                                    mTask = null;
                                }
                            });
                        }
                    }
                });
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        FFMpegUtils.killProcesses();
        if (mTask != null) {
            mTask.cancel(true);
            mTask = null;
        }
        EditingVideoActivity.this.finish();
        startActivity(new Intent(EditingVideoActivity.this, RecordingVideoActivity.class));
    }

    private String getTopTailVideoExtension(boolean flagTop) {
        try {
            int pos = MainApplication.getInstance().getSelectedTemplePosition();
            if (MainApplication.getInstance().getTemplateArray() != null && MainApplication.getInstance().getTemplateArray().length() > 0) {
                JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
                // for (int i = jsonArray.length() - 1; i >= 0; i--) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    options1Items.add(new ProvinceBean(i, jsonObject.optString("title"), jsonObject.optString("directory"), ""));
                }
            }
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
            if (flagTop) {
                return jsonArray.getJSONObject(pos).optJSONObject("data").getJSONObject("brand-logo").getString("video_top");
            } else {
                return jsonArray.getJSONObject(pos).optJSONObject("data").getJSONObject("brand-logo").getString("video_tail");
            }

        }catch (Exception e) {

        }
        return  "";
    }
    public void getTopandTailThumbnailUrl(){
        try{

            String thumbnailUrlDirectory = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"VideoEditorApp/"+MainApplication.getInstance().getTemplate().strDirectoryID;
            File fileTop = new File(thumbnailUrlDirectory, "top_thumbnail.png");
            File fileTail = new File(thumbnailUrlDirectory, "tail_thumbnail.png");

            if(fileTop.exists()){
                Bitmap bmTopThumb =null;
                bmTopThumb = getPreview(Uri.fromFile(fileTop));

                if (bmTopThumb != null) {
                    imgThumbVideo2.setImageBitmap(bmTopThumb);
                }
            }


            if (fileTail.exists()){
                Bitmap bmTailThumb;
                bmTailThumb = getPreview(Uri.fromFile(fileTail));
                if (bmTailThumb != null) {
                    imgThumbVideo3.setImageBitmap(bmTailThumb);
                }
            }




        }catch (Exception e){
            e.printStackTrace();
        }
    }
    Bitmap getPreview(Uri uri) {
        File image = new File(uri.getPath());

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(image.getPath(), bounds);
        if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
            return null;

        int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
                : bounds.outWidth;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = 4;
        return BitmapFactory.decodeFile(image.getPath(), opts);
    }
    public void downloadTopVideo() {

        try {
            int pos = MainApplication.getInstance().getSelectedTemplePosition();
            String extenstion = getTopTailVideoExtension(true);

            if (extenstion != null && !extenstion.equalsIgnoreCase("")) {
                findViewById(R.id.pb_Top).setVisibility(View.VISIBLE);
                TopDownloader fileDownloader = new TopDownloader(EditingVideoActivity.this, getTemplateUrl((int) options1Items.get(pos).getId(), "top"), extenstion, options1Items.get(pos).getDirectoryId());
                fileDownloader.startDownload(new TopVideoDownload() {
                    @Override
                    public void getTopVideoUrl(final String url) {
                        Constant.setDownloadTopVideo(url);
                        flagTopVideoDownloaded = true;
                        convertTopVideoVideoFormat();
                    }
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void downloadThumbnail() {
        try {
            int pos = MainApplication.getInstance().getSelectedTemplePosition();
            if (MainApplication.getInstance().getTemplateArray() != null && MainApplication.getInstance().getTemplateArray().length() > 0) {
                JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    options1Items.add(new ProvinceBean(i, jsonObject.optString("title"), jsonObject.optString("directory"), ""));
                }
            }
            findViewById(R.id.pb_Thumb).setVisibility(View.VISIBLE);
            FileDownloader fileDownloader = new FileDownloader(EditingVideoActivity.this, getThumbnailUrl((int) options1Items.get(pos).getId(), "tail"), options1Items.get(pos).getDirectoryId());
            fileDownloader.startDownload(new TopVideoDownload() {
                @Override
                public void getTopVideoUrl(String url) {
                    findViewById(R.id.pb_Thumb).setVisibility(View.GONE);
                    Constant.setThumbanulImageUrl(url);
                    thumbNailUrl = url;
                    initializeThumbView();
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void downloadTailVideo() {
        try {
            int pos = MainApplication.getInstance().getSelectedTemplePosition();
            String extenstion = getTopTailVideoExtension(false);

            if (extenstion != null && !extenstion.equalsIgnoreCase("")) {
                findViewById(R.id.pb_Tail).setVisibility(View.VISIBLE);
                flagTailVideoDownloaded = false;
                TailDownloader fileDownloader = new TailDownloader(EditingVideoActivity.this, getTemplateUrl((int) options1Items.get(pos).getId(), "tail"), extenstion, options1Items.get(pos).getDirectoryId());
                fileDownloader.startDownload(new TopVideoDownload() {
                    @Override
                    public void getTopVideoUrl(final String url) {
                        flagTailVideoDownloaded = true;
                        Constant.setDownloadTailVideo(url);
                        convertTopVideoVideoFormat();
                    }
                });
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface TopVideoDownload {
        public void getTopVideoUrl(String url);
    }

    private String getThumbnailUrl(int postion, String topTail) {
        StringBuilder builder = new StringBuilder();
        http:
//syd.static.videomyjob.com/profile/6P9UFyOSl8_5_lg.jpg"

        try {
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
            builder.append("http://")
                    .append(getServer(sharedPreferenceWriter.getString(SPreferenceKey.REGION)))
                    .append("/profile/")
                    .append(sharedPreferenceWriter.getString(SPreferenceKey.PEPPER_ID))
                    .append("_" + sharedPreferenceWriter.getString(SPreferenceKey.USERID))
                    .append("_" + "lg.jpg");

        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    private String getTemplateUrl(int postion, String topTail) {
        StringBuilder builder = new StringBuilder();

        try {
            JSONArray jsonArray = MainApplication.getInstance().getTemplateArray();
            builder.append("http://")
                    .append(getServer(sharedPreferenceWriter.getString(SPreferenceKey.REGION)))
                    .append("/company/")
                    .append(sharedPreferenceWriter.getString(SPreferenceKey.COMPANY_DIRECTORY))
                    .append("/" + jsonArray.getJSONObject(postion).optString("directory"))
                    .append("/" + topTail + "." + jsonArray.getJSONObject(postion).optJSONObject("data").getJSONObject("brand-logo").getString("video_top"));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString();
    }

    public String getServer(String company_region) {
        String toReturn = "syd.static.videomyjob.com";

        //Should we ever change the default
        if (company_region == "AU") {
            toReturn = "syd.static.videomyjob.com";
        } else if (company_region == "HN") {
            toReturn = "hn.static.videomyjob.com";
        }

        return toReturn;
    }


    private void initializeButtons() {
        UITouchButton.applyEffect(btnNext, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        if (!flagPlay) {
                            currentVideoSeekPosition = MainApplication.getInstance().getVideoEnd();

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
                            currentVideoSeekPosition = MainApplication.getInstance().getVideoStart();

                            setCurrentSeekTime(currentVideoSeekPosition);
                        }
                    }
                });

        UITouchButton.applyEffect(((View)findViewById(R.id.llBack)), UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        FFMpegUtils.killProcesses();
                        if (mTask != null) {
                            mTask.cancel(true);
                            mTask = null;
                        }
                        EditingVideoActivity.this.finish();
                        startActivity(new Intent(EditingVideoActivity.this, RecordingVideoActivity.class));
                    }
                });

        UITouchButton.applyEffect(btnEncodeVideo, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {

                        if(flagTopVideoDownloaded && flagTailVideoDownloaded){
                            if (flagTopVideoConverted && flagTailVideoConverted) {
                                if (((TextView) findViewById(R.id.txt_job_title)).getText().toString().length() > 0) {
                                    convertOverlaysPNG();
                                } else {
                                    showAlert(R.string.str_alert_title_information, "Please enter video title.", "Ok");
                                }

                            } else {
                                // convertOverlaysPNG();
                                showAlert(R.string.str_alert_title_information, "Please wait until the top/tail videos have been processed.", "Ok");

                            }
                        }else{
                            showAlert(R.string.str_alert_title_information, "Please wait until the top/tail videos have been processed.", "Ok");
                        }




                    }
                });
        UITouchButton.applyEffect(imgThumbVideo1, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_VIEW);
                        intent.setDataAndType(Uri.fromFile(new File(thumbNailUrl)), "image/*");
                        startActivity(intent);
                    }
                });
        UITouchButton.applyEffect(imgThumbVideo2, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        if (FileUtils.isExistFile(Constant.getTopVideo()) && flagTopVideoConverted) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.getTopVideo()));
                            intent.setDataAndType(Uri.parse(Constant.getTopVideo()), "video/mp4");
                            startActivity(intent);
                        }
                    }
                });
        UITouchButton.applyEffect(imgThumbVideo3, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        if (FileUtils.isExistFile(Constant.getTailVideo()) && flagTailVideoConverted) {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constant.getTailVideo()));
                            intent.setDataAndType(Uri.parse(Constant.getTailVideo()), "video/mp4");
                            startActivity(intent);
                        }
                    }
                });
        UITouchButton.applyEffect(imgThumbVideo4, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        getSid();
                    }
                });

        UITouchButton.applyEffect(layout_job_title, UITouchButton.EFFECT_ALPHA, Constant.BUTTON_NORMAL_ALPHA,
                Constant.BUTTON_FOCUS_ALPHA, new Runnable() {
                    @Override
                    public void run() {
                        addTitle();
                    }
                });

        videoThumbsLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float seekVideoTime = ((float) event.getX() / (float) Constant.SP_PER_SECOND / getDisplayMetric().scaledDensity);
                setCurrentSeekTime(seekVideoTime * 1000.f);
                v.onTouchEvent(event);
                return true;
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

        setOnVideoIndicatorListener();
        setOnVideoTrimTouchListener();
    }

    private int lastXTrimLeft = 0;
    private int lastXTrimRight = 0;
    private int trimWidthOnTap = 0;

    private void setOnVideoTrimTouchListener() {
        final float trimBarWidth = trimImageViewWidth * getDisplayMetric().scaledDensity;
        btnTrimLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastXTrimLeft = X;
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) video_trim_left.getLayoutParams();
                    trimWidthOnTap = lParams.width;
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (Math.abs(lastXTrimLeft - X) > Constant.SP_PER_SECOND / 10) {
                        FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) video_trim_left.getLayoutParams();
                        lParams.width = (int) (trimWidthOnTap + (X - lastXTrimLeft));
                        if (lParams.width < trimBarWidth) {
                            lParams.width = (int) trimBarWidth;
                        }
                        int trimStart = (int) ((lParams.width - trimBarWidth) / (float) Constant.SP_PER_SECOND / getDisplayMetric().scaledDensity * 1000);
                        if (trimStart + 1000 < MainApplication.getInstance().getVideoEnd()) {
                            video_trim_left.setLayoutParams(lParams);
                            MainApplication.getInstance().setVideoStart(trimStart);
                            updateTimelineAfterTrim();
                        }

                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return true;
            }
        });

        btnTrimRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    lastXTrimRight = X;
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) video_trim_right.getLayoutParams();
                    trimWidthOnTap = lParams.width;
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    Log.d("Width", Integer.toString(trimWidthOnTap) + " " + Integer.toString(X));
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (Math.abs(lastXTrimRight - X) > Constant.SP_PER_SECOND / 10) {
                        FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) video_trim_right.getLayoutParams();
                        lParams.width = (int) (trimWidthOnTap + (lastXTrimRight - X));
                        if (lParams.width < trimBarWidth) {
                            lParams.width = (int) trimBarWidth;
                        }

                        int trimEnd = MainApplication.getInstance().getVideoLength() - (int) ((lParams.width - trimBarWidth ) / (float) Constant.SP_PER_SECOND / getDisplayMetric().scaledDensity * 1000);
                        if (trimEnd > MainApplication.getInstance().getVideoStart() + 1000) {
                            int parentWidth = ((View) video_trim_right.getParent()).getWidth();
                            lParams.leftMargin = parentWidth - lParams.width;
                            video_trim_right.setLayoutParams(lParams);

                            MainApplication.getInstance().setVideoEnd(trimEnd);
                            updateTimelineAfterTrim();
                        }
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return true;
            }
        });
    }

    /**
     * drag video indicator on timeline view
     */
    private float lastVideoIndicatorTapPosition = 0;
    private int _xDelta = 0;

    private void setOnVideoIndicatorListener() {
        final float indicatorWidth = (videoIndicatorWidth * getDisplayMetric().scaledDensity);
        final float trimBarWidth = trimImageViewWidth * getDisplayMetric().scaledDensity;
        video_indicator.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int X = (int) event.getRawX();

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    FrameLayout.LayoutParams lParams = (FrameLayout.LayoutParams) video_indicator.getLayoutParams();
                    _xDelta = X - lParams.leftMargin;
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                }

                if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float currentPosition = event.getX();
                    if (Math.abs(lastVideoIndicatorTapPosition - currentPosition) > Constant.SP_PER_SECOND / 10) {
                        float time = (float) (X - _xDelta + indicatorWidth / 2 - trimBarWidth) / (float) Constant.SP_PER_SECOND / getDisplayMetric().scaledDensity * 1000.f;
                        setCurrentSeekTime(time);
                        lastVideoIndicatorTapPosition = currentPosition;
                    }
                }

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                }
                return true;
            }
        });
    }

    /**
     * show tutorial page via webview
     */
  /*  private void showTutorialPage() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://live.videomyjob.com"));
        startActivity(browserIntent);
    }*/

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            setCurrentSeekTime(videoView.getCurrentPosition());
            mHandler.postDelayed(this, 100);
        }
    };

    /**
     * set current video 's seek time.
     *
     * @param time
     */
    public void setCurrentSeekTime(float time) {
        int trimStart = MainApplication.getInstance().getVideoStart();
        int trimEnd = MainApplication.getInstance().getVideoEnd();
        if (time >= trimStart && time <= trimEnd) {
            final float trimBarWidth = trimImageViewWidth * getDisplayMetric().scaledDensity;
            currentVideoSeekPosition = (int) time;

            if (!flagPlay) {
                videoView.seekTo(currentVideoSeekPosition);
                videoView.pause();
            }
            float width = (videoIndicatorWidth * getDisplayMetric().scaledDensity);
            float marginLeft = (float) time / 1000.f * Constant.SP_PER_SECOND * (getDisplayMetric().scaledDensity) - width / 2 + trimBarWidth;
            FrameLayout.LayoutParams param = new FrameLayout.LayoutParams((int) width, FrameLayout.LayoutParams.MATCH_PARENT);
            param.setMargins((int) marginLeft, 0, 0, 0);
            video_indicator.setLayoutParams(param);
            updateOverlayView((float) time / 1000.f);
        } else {
            if (time >= trimEnd) {
                if (flagPlay) {
                    videoView.pause();
                    videoView.seekTo(trimStart);
                    onPlay();
                }
            }
        }
    }

    /**
     * update overlay view during play or once timeline is selected
     */
    public void updateOverlayView(float time) {
        overlayView.setCurrentVideoTime(time);
    }

    public void updateOverlayView() {
        overlayView.invalidate();
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

    private void updateVideoDuration() {
        int start = MainApplication.getInstance().getVideoStart();
        int end = MainApplication.getInstance().getVideoEnd();

        int topLength = 0;
        if (FileUtils.isExistFile(Constant.getTopVideo())) {
            topLength = VideoUtils.getVideoLength(Constant.getTopVideo());
        }

        int tailLength = 0;
        if (FileUtils.isExistFile(Constant.getTailVideo())) {
            tailLength = VideoUtils.getVideoLength(Constant.getTailVideo());
        }
        txtVideoDuration.setText(getString(R.string.label_total_length) + StringUtils.getMinuteSecondString((end - start) / 1000 + topLength + tailLength, false));
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
                int videoLength = videoView.getDuration();
                MainApplication.getInstance().setVideoLength(videoLength);
                videoView.seekTo(defaultVideoInitialTime);

                videoControlLayout.setVisibility(View.VISIBLE);

                if (!flagFromBackground && !flagTimelineInitialized) {
                    MainApplication.getInstance().setVideoStart(0);
                    MainApplication.getInstance().setVideoEnd(videoLength);
                }

                if (!flagTimelineInitialized) {
                    initializeTimeLineView();
                }
                updateVideoDuration();
                setCurrentSeekTime(0);
                titleThumbsLayout.setTrimLeftRight(0, videoLength);
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (FileUtils.isExistFile(Constant.getTailVideo())) {
                    Bitmap bmTailThumb;
                    bmTailThumb = ThumbnailUtils.createVideoThumbnail(Constant.getTailVideo(), MediaStore.Video.Thumbnails.MINI_KIND);
                    if (bmTailThumb != null) {
                        imgThumbVideo3.setImageBitmap(bmTailThumb);
                    }
                }

                if (FileUtils.isExistFile(Constant.getTopVideo())) {
                    Bitmap bmTopThumb;
                    bmTopThumb = ThumbnailUtils.createVideoThumbnail(Constant.getTopVideo(), MediaStore.Video.Thumbnails.MINI_KIND);
                    if (bmTopThumb != null) {
                        imgThumbVideo2.setImageBitmap(bmTopThumb);
                    }
                }

                if (null!=thumbNailUrl && thumbNailUrl.length() > 0) {
                    File imgFile = new File(thumbNailUrl);
                    if (imgFile.exists()) {
                        Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                       Glide.with(EditingVideoActivity.this).load(imgFile).transform(new GlideCircleTransform(EditingVideoActivity.this)).into((ImageView) findViewById(R.id.img_thumb_video1));
                        File fileOFTemplete = new File(Environment.getExternalStorageDirectory() + "/VideoEditorApp/"+MainApplication.getInstance().getTemplate().strDirectoryID) ;
                        File file = new File(fileOFTemplete, "brand.png");
                        Bitmap brandBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                        ((ImageView)findViewById(R.id.iv_BrandImage)).setImageBitmap(brandBitmap);
                    }
                }

                updateVideoDuration();
            }
        });
    }

    /**
     * Convert overlay into png && save video overlay information..
     */
    private void convertOverlaysPNG() {
        ArrayList<VideoOverlay> videoOverlayInformation = MainApplication.getInstance().getVideoOverlayInformation();
        videoOverlayInformation.clear();

        int videoWidth = Constant.VIDEO_WIDTH;
        int videoHeight = Constant.VIDEO_HEIGHT;

        ArrayList<ChildTextTimelineLayout> titleList = MainApplication.getTimelineTitlesInformation();
        OverlayBean overlayBean = MainApplication.getInstance().getTemplate();

        //convert brand overlay into png

        int fileIndex = 0;
        if (overlayBean.brandLogo != null && overlayBean.brandLogo.backgroundImage.length() > 0) {
            OverlayBean.Overlay brandOverlay = overlayBean.brandLogo;
            String fileName = Constant.getOverlayDirectory() + "0.png";
            overlayView.convertOverlayToPNG("", brandOverlay, videoWidth, videoHeight, fileName);
            int x = (int) (videoWidth * (brandOverlay.x / 100.f));
            int y = (int) (videoHeight * (brandOverlay.y / 100.f));
            if (FileUtils.isExistFile(fileName)) {
                VideoOverlay info = new VideoOverlay(0, MainApplication.getInstance().getVideoLength(), x, y, fileName);
                videoOverlayInformation.add(info);
                fileIndex = 1;
            }

        }

        //convert caption overlay into image.
        for (int i = 0; i < titleList.size(); i++) {
            ChildTextTimelineLayout title = titleList.get(i);
            String fileName = Constant.getOverlayDirectory() + (fileIndex) + ".png";
            overlayView.convertOverlayToPNG(title.getTitleText(), title.getCaptionOverlay(), videoWidth, videoHeight, fileName);

            int x = (int) (videoWidth * (title.getCaptionOverlay().x / 100.f));
            int y = (int) (videoHeight * (title.getCaptionOverlay().y / 100.f));
            if (FileUtils.isExistFile(fileName)) {
                VideoOverlay info = new VideoOverlay(title.getStartTime(), title.getEndTime(), x, y, fileName);
                videoOverlayInformation.add(info);
                fileIndex++;
            }
        }

        showActivity(UploadingVideoScreen.class, editJobTitle.getText().toString());
        return;

    }

    /**
     * highlight video image in timeline area.
     *
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
                  /*  progressDialog.setMessage("Preparing video...");
                    progressDialog.show();*/
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
                final int videoLength = MainApplication.getInstance().getVideoLength();
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
                                ImageView imageView = (ImageView) thumbImageLayout.findViewById(R.id.img_thumb);
                                if ( (int) (videoLength / 1000) - tagObj <= Constant.TIMELINE_UNIT_SECOND) {
                                    float offset = (float) videoLength / 1000 - (float) tagObj;
                                    int width = (int) (offset * 60 * getDisplayMetric().scaledDensity / Constant.TIMELINE_UNIT_SECOND);
                                    LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(width, LinearLayout.LayoutParams.MATCH_PARENT);
                                    imageView.setLayoutParams(param);
                                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                                }

                                if (bitmap != null && imageView != null) {
                                    LinearLayout.LayoutParams param = (LinearLayout.LayoutParams)imageView.getLayoutParams();
                                    Bitmap resizedBitmap = BitmapUtils.resizeBitmap(bitmap, param.width);
                                    imageView.setImageBitmap(resizedBitmap);
                                    thumbImageLayout.setTag(tagObj / Constant.TIMELINE_UNIT_SECOND);
                                    videoThumbsLayout.addView(thumbImageLayout);

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
                for (int i = 0; i < frameGains.length; i++) {
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
            } catch (Exception e) {

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
                }
            });

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            try {

               // progressDialog.dismiss();
                flagProgressDialogIsRunning = false;
                flagTailVideoDownloaded = flagTopVideoDownloaded = true;
                flagTopVideoConverted = flagTailVideoConverted = true;
                downloadThumbnail();

                String extenstion = getTopTailVideoExtension(true);
                if (extenstion != null && !extenstion.equalsIgnoreCase("")) {
                    if (!Constant.flagDebug) {
                        flagTopVideoConverted = false;
                        flagTopVideoDownloaded = false;
                    }
                    findViewById(R.id.layout_no_top).setVisibility(View.GONE);
                }

                extenstion = getTopTailVideoExtension(false);
                if (extenstion != null && !extenstion.equalsIgnoreCase("")) {
                    findViewById(R.id.layout_no_tail).setVisibility(View.GONE);
                    if (!Constant.flagDebug) {
                        flagTailVideoConverted = false;
                        flagTailVideoDownloaded = false;
                    }
                }
                if (!Constant.flagDebug) {
                    downloadTopVideo();
                    downloadTailVideo();
                }

                getTopandTailThumbnailUrl();
            } catch (Exception e) {

            }
        }
    }

    private static ArrayList<ChildTextTimelineLayout> cachedInfo = null;

    private void restoreTimelineLayout() {
        titleThumbsLayout.removeChildTitleLayouts();
        if (cachedInfo != null) {
            for (int i = 0; i < cachedInfo.size(); i++) {
                ChildTextTimelineLayout cachedItem = cachedInfo.get(i);
                titleThumbsLayout.addNewTitleInformation(cachedItem.getTitleText(), cachedItem.getStartTime(), cachedItem.getEndTime(), cachedItem.getCaptionOverlay(), cachedItem.isRemovable());
            }
        }
        updateOverlayView(0);
    }

    /**
     *
     */
    private void addDefaultOverlays() {
        //add default captions into timeline view (contact, name overlay)

        OverlayBean template = MainApplication.getInstance().getTemplate();
        OverlayBean.Overlay nameOverlay = template.name;
        if (nameOverlay != null) {
            titleThumbsLayout.addNewTitleInformation(nameOverlay.defaultText, 0, Constant.TIMELINE_UNIT_SECOND, nameOverlay, false);
        }

        OverlayBean.Overlay contactOverlay = template.contact;
        if (contactOverlay != null) {
            float lastSec = (float) MainApplication.getInstance().getVideoLength() / 1000;
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


    public void getSid() {
        try {
            if (CheckNetworkConnection.isNetworkAvailable(EditingVideoActivity.this)) {
                List<NameValuePair> paramePairs = new ArrayList<NameValuePair>();
                RequestBean requestBean = new RequestBean();
                requestBean.setActivity(EditingVideoActivity.this);
                requestBean.setUrl("get_sid.php");
                requestBean.setParams(paramePairs);
                requestBean.setIsProgressBarEnable(true);
                RequestHandler requestHandler = new RequestHandler(requestBean, requestSid);
                requestHandler.execute(null, null, null);
            } else {
                CustomDialogs.showOkDialog(EditingVideoActivity.this, "Please check network connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private RequestListner requestSid = new RequestListner() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            try {
                String sid = "";
                String url = "";
                if (jsonObject != null) {
                    if (!jsonObject.isNull("sid")) {
                        sid = jsonObject.getString("sid");
                    }

                    url = "https://live.videomyjob.com/api/app_login.php?user_id=" + SharedPreferenceWriter.getInstance().getString(SPreferenceKey.USERID) + " & sid=" + sid + " & " + "redirect=3";


                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };


    BroadcastReceiver finishActivity = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            finish();

        }
    };


}
