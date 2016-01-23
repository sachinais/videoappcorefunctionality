package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.nick.sampleffmpeg.R;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FontTypeface;

/**
 * Created by Admin on 1/23/2016.
 */
public class SelectPlatfom extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_platform);
        setFonts();
    }


   private void setFonts() {
        ((TextView)findViewById(R.id.tv_CompanyAccount)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_SEMIBOLD));
        ((TextView)findViewById(R.id.tv_compnyfacebook)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tv_compnyLinkedIn)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tv_compnyLinkedIn)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));

        ((TextView)findViewById(R.id.tv_PersonalAccount)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tv_PersonalFacebook)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tv_PersonalLinedIn)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));
        ((TextView)findViewById(R.id.tv_PersonalTwitter)).setTypeface(FontTypeface.getTypeface(SelectPlatfom.this, AppConstants.FONT_SUFI_REGULAR));

    }
}
