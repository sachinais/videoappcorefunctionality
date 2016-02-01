package com.nick.sampleffmpeg.ui.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.utils.ffmpeg.FFMpegUtils;

/**
 * Created by baebae on 12/22/15.
 */
public class BaseActivity extends Activity {
    protected DisplayMetrics mDisplayMetrics;

    protected ProgressDialog progressDialog;
    /**
     * set dimension for multi-screen size
     * @param layoutResID
     */
    @Override
    public void setContentView (int layoutResID) {
        mDisplayMetrics = getResources().getDisplayMetrics();
        mDisplayMetrics.scaledDensity = mDisplayMetrics.heightPixels / 400.0f;
        Constant.setScaleDensity(mDisplayMetrics.scaledDensity);
        super.setContentView(layoutResID);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(null);
        progressDialog.setCanceledOnTouchOutside(false);


        FFMpegUtils.initializeFFmpeg(this);
    }

    public DisplayMetrics getDisplayMetric() {
        return mDisplayMetrics;
    }
    /**
     * show another activity from current activity
     * @param activity
     * @param parameters
     */
    public void showActivity(Class<?> activity, String ... parameters) {
        Intent intent = new Intent();
        intent.setClass(this, activity);
        if (parameters != null) {
            for(int i = 0; i < parameters.length; i++){
                String field = "parameter" + Integer.toString(i + 1);
                intent.putExtra(field, parameters[i]);
            }
        }
        startActivity(intent);
    }

    /**
     * get pixel width from sp
     * @param sp input sp
     * @return calculated pixel for device
     */
    public int getPixelFromDensity(double sp) {
        return (int)(sp * mDisplayMetrics.scaledDensity);
    }

    /**
     * init dialog postive/negative button event, title, etc
     * @param alertDialogBuilder alert dialog builder handle object
     * @param parameters parameter format : positive button name(string), positive button event(runnable), negative button name(string), negative button event(runnable)
     */

    public void initDialogButtons(AlertDialog.Builder alertDialogBuilder, Object ... parameters) {
        Runnable runnable = null;
        if (parameters.length > 0) {
            String strPositiveButton = (String)parameters[0];
            if (parameters.length > 1) {
                runnable = (Runnable)parameters[1];
            }
            final Runnable runnablePositive = runnable;

            if (strPositiveButton != null && strPositiveButton.length() > 0) {
                alertDialogBuilder.setPositiveButton(strPositiveButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                if (runnablePositive != null) {
                                    runnablePositive.run();
                                }
                                dialog.dismiss();
                                currentAlertDialog = null;
                            }
                        });
            }
        }

        if (parameters.length > 2) {
            runnable = null;
            String strNegativeButton = (String)parameters[2];
            if (parameters.length > 3) {
                runnable = (Runnable)parameters[3];
            }
            final Runnable runnableNegative = runnable;

            if (strNegativeButton != null && strNegativeButton.length() > 0) {
                alertDialogBuilder.setNegativeButton(strNegativeButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (runnableNegative != null) {
                                    runnableNegative.run();
                                }
                                dialog.dismiss();
                                currentAlertDialog = null;
                            }
                        });
            }
        }

        if (parameters.length > 4) {
            runnable = null;
            String strNegativeButton = (String)parameters[4];
            if (parameters.length > 5) {
                runnable = (Runnable)parameters[5];
            }
            final Runnable runnableNegative = runnable;

            if (strNegativeButton != null && strNegativeButton.length() > 0) {
                alertDialogBuilder.setNeutralButton(strNegativeButton,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (runnableNegative != null) {
                                    runnableNegative.run();
                                }
                                dialog.dismiss();
                                currentAlertDialog = null;
                            }
                        });
            }
        }
    }
    protected AlertDialog currentAlertDialog = null;

    public AlertDialog getCurrentAlertDialog() {
        return currentAlertDialog;
    }

    public void closeCurrentDialog() {
        if (currentAlertDialog != null) {
            currentAlertDialog.dismiss();
            currentAlertDialog = null;
        }
    }
    /**
     * show dialog from layout on activity
     * @param resID resource id
     * @param parameters parameter format : positive button name(string), positive button event(runnable), negative button name(string), negative button event(runnable)
     * @return View dialog view
     */
    public View showViewContentDialog(int resID, Object ... parameters)
    {
        LayoutInflater layoutInflater = LayoutInflater.from(this);
        View view = layoutInflater.inflate(resID, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set prompts.xml to alert dialog builder
        alertDialogBuilder.setView(view);
        alertDialogBuilder.setCancelable(false);

        //Init dialog buttons and event
        initDialogButtons(alertDialogBuilder, parameters);
        // create alert dialog
        currentAlertDialog = alertDialogBuilder.create();
        // show it
        currentAlertDialog.show();

        return view;
    }

    /**
     * show alert dialog
     * @param titleID alert dialog title string id
     * @param contentID alert dialog content string id
     * @param parameters parameter format : positive button name(string), positive button event(runnable), negative button name(string), negative button event(runnable)
     */
    public void showAlert(int titleID, int contentID, Object ... parameters) {
        showAlert(getString(titleID), getString(contentID), parameters);
    }
    /**
     * show alert dialog
     * @param titleID alert dialog title string id
     * @param strContent alert dialog content string
     * @param parameters parameter format : positive button name(string), positive button event(runnable), negative button name(string), negative button event(runnable)
     */
    public void showAlert(int titleID, String strContent, Object ... parameters) {
        showAlert(getString(titleID), strContent, parameters);
    }
    /**
     * show alert dialog
     * @param strTitle alert dialog title string
     * @param strContent alert dialog content string
     * @param parameters parameter format : positive button name(string), positive button event(runnable), negative button name(string), negative button event(runnable)
     */
    public void showAlert(String strTitle, String strContent, Object ... parameters) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(strTitle);
        alertDialogBuilder
                .setMessage(strContent)
                .setCancelable(false);

        initDialogButtons(alertDialogBuilder, parameters);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
