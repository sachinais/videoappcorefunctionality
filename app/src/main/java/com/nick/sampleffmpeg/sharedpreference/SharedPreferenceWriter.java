package com.nick.sampleffmpeg.sharedpreference;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class SharedPreferenceWriter {
	private static Context context = null;
	private static SharedPreferenceWriter sharePref = null;
	private static SharedPreferences mPrefs;
	private static SharedPreferences.Editor prefEditor = null;

	public static SharedPreferenceWriter getInstance(Context c) {
		context = c;
		if (null == sharePref) {
			sharePref = new SharedPreferenceWriter();
			mPrefs = context.getSharedPreferences("VIDEO_EDITOR", Context.MODE_PRIVATE);
			prefEditor = mPrefs.edit();
		}
		return sharePref;
	}

	public static SharedPreferenceWriter getInstance() {
		return sharePref;
	}

	private SharedPreferenceWriter() {
		// TODO Auto-generated constructor stub
	}
	public void writeStringValue(String key, String value) {

		prefEditor.putString(key, value);
		prefEditor.commit();
	}

	public void writeIntValue(String key, int value) {

		prefEditor.putInt(key, value);
		prefEditor.commit();
	}

	public void writeBooleanValue(String key, boolean value) {

		prefEditor.putBoolean(key, value);
		prefEditor.commit();
	}

	public void writeLongValue(String key, long value) {

		prefEditor.putLong(key, value);
		prefEditor.commit();
	}

	public void clearPreferenceValue(String key, String value) {

		prefEditor.putString(key, "");
		prefEditor.commit();

	}

	public void clearPreferenceValue(String key) {
		prefEditor.remove(key);
		boolean value = prefEditor.commit();
		Log.d("Str", value+"");
	}


	public String getString(String key) {

		return mPrefs.getString(key, "");

	}

	public int getInt(String key) {

		return mPrefs.getInt(key, 0);
	}

	public boolean getBoolean(String key) {

		return mPrefs.getBoolean(key, false);
	}

	public long getLong(String key) {

		return mPrefs.getLong(key, (long) 0.0);
	}

	public void clearPreferenceValues() {
		prefEditor.clear().commit();
	}
}
