package com.nick.sampleffmpeg.utils;

import android.content.Context;
import android.graphics.Typeface;

import java.util.Hashtable;

public class FontTypeface {

	private static Hashtable<String, Typeface> fontCache = new Hashtable<String, Typeface>();

    public static Typeface getTypeface(Context context , String name) {
        Typeface tf = fontCache.get(name);
        if(tf == null) {
            try {
                tf = Typeface.createFromAsset(context.getAssets(), name);
            }
            catch (Exception e) {
                return null;
            }
            fontCache.put(name, tf);
        }
        return tf;
    }
    
   /* public static SpannableString getTitle(Context c,CharSequence title,String typeface) {
	    if (title.length()>0) {
	    	Typeface fontType = FontTypeface.get(typeface, c);
		    SpannableString s = new SpannableString(title);
		    s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		    s.setSpan(new ActionBarFontStyle("", fontType), 0, s.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    return s;
		}else {
			Typeface fontType = FontTypeface.get(typeface, c);
		    SpannableString s = new SpannableString("");
		    s.setSpan(new ForegroundColorSpan(Color.WHITE), 0, s.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
		    s.setSpan(new ActionBarFontStyle("", fontType), 0, s.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		    return s;
			
		}
	    
	}*/
}