package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.nick.sampleffmpeg.MainApplication;
import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.utils.GlideCircleTransform;

import java.io.File;

/**
 * Created by Admin on 1/31/2016.
 */
public class FullViewThumbNail extends Activity {

    ImageView iv_BrandImage;
    ImageView img_thumb_video1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
     //   setContentView(R.la);
        setContentView(R.layout.thumbnail_full_screen);
        initViwes();


    }


    public void initViwes(){
        try{
            String thumbNailUrl= getIntent().getStringExtra("Url");


            iv_BrandImage = (ImageView)findViewById(R.id.iv_BrandImage);
            img_thumb_video1 = (ImageView)findViewById(R.id.img_thumb_video1);
            File imgFile = new File(thumbNailUrl);
            if (imgFile.exists()) {
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                Glide.with(FullViewThumbNail.this).load(imgFile).transform(new GlideCircleTransform(FullViewThumbNail.this)).into((ImageView) findViewById(R.id.img_thumb_video1));
                File fileOFTemplete = new File(Environment.getExternalStorageDirectory() + "/VideoEditorApp/"+ MainApplication.getInstance().getTemplate().strDirectoryID) ;
                File file = new File(fileOFTemplete, "brand.png");
                Bitmap brandBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                ((ImageView)findViewById(R.id.iv_BrandImage)).setImageBitmap(brandBitmap);
            }
            findViewById(R.id.llBack).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


}
