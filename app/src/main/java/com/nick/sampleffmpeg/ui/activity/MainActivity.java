package com.nick.sampleffmpeg.ui.activity;

/**
 * Created by Admin on 1/21/2016.
 */import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.nick.sampleffmpeg.R;


public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private static final int CAPTURE_RETURN = 1;
    private static final int GALLERY_RETURN = 2;
    private static final int SUBMIT_RETURN = 3;

    private ProgressBar progressBar;

    private Button btnCaptureVideo;
    private Button btnSelectFromGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);

      /*  YouTubeService service = new YouTubeService("project id on console.developer.google.com","androidkey");
        service.setUserCredentials("yourYouTubeAccount@gmail.com", "yourPassword");
        VideoEntry newEntry = new VideoEntry();
        YouTubeMediaGroup mg = newEntry.getOrCreateMediaGroup();
        mg.setTitle(new MediaTitle());
        mg.getTitle().setPlainTextContent("Video Title");
        mg.addCategory(new MediaCategory(YouTubeNamespace.CATEGORY_SCHEME, "Tech"));
        mg.setKeywords(new MediaKeywords());
        mg.getKeywords().addKeyword("anyKeyword");
        mg.setDescription(new MediaDescription());
        mg.getDescription().setPlainTextContent("VIDEO DESCRIPTION");
        mg.setPrivate(false);
        mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, "mydevtag"));
        mg.addCategory(new MediaCategory(YouTubeNamespace.DEVELOPER_TAG_SCHEME, "anotherdevtag"));
        MediaFileSource ms = new MediaFileSource(videoFileToUpload, "video/quicktime");
        newEntry.setMediaSource(ms);
        VideoEntry createdEntry = service.insert(new URL(Constant.YOUTUBE_UPLOAD_URL), newEntry);
        Log.v("TAG", "VIDEO INSERTED ID : " + createdEntry.getId());*/

    }

}