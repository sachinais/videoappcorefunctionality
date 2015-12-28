package com.nick.sampleffmpeg.ui.control;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by baebae on 12/22/15.
 */
public class UITouchButton {
    public static final int		EFFECT_COLOR 		= 0;
    public static final int		EFFECT_IMAGE 		= 1;
    public static final int		EFFECT_FONTCOLOR 	= 2;
    public static final int		EFFECT_ALPHA 		= 3;
    public static final int		EFFECT_NONE			= 4;

    public class TouchEnable {
        public TouchEnable(int value)
        {
            this.value = value;
        }
        public int value = 0;
    }
    /**
     * make a view as a button (View or ImageView)
     */
    public static void applyEffect(final View view, final int type, final int backgroundNormal, final int backgroundFocus, final Runnable onClickListener) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x = event.getX();
                float y = event.getY();
                Object tagObject = view.getTag();
                if (tagObject instanceof TouchEnable) {
                    if (((TouchEnable)tagObject).value == 100) {
                        return true;
                    }
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        UITouchButton.setViewBackground(view, type, backgroundFocus);
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (checkLocalPointInView(view, x, y)) {
                            UITouchButton.setViewBackground(view, type, backgroundFocus);
                        }
                        else {
                            UITouchButton.setViewBackground(view, type, backgroundNormal);
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        UITouchButton.setViewBackground(view, type, backgroundNormal);

                        if (onClickListener != null) {
                            if (checkLocalPointInView(view, x, y)) {
                                onClickListener.run();
                            }
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        UITouchButton.setViewBackground(view, type, backgroundNormal);
                        break;
                }

                v.onTouchEvent(event);
                return true;
            }
        });
    }

    /*
     Get the padding of view.
      */
    public static int[] getPaddingOfView(View view) {
        int[] padding = { 0, 0, 0, 0 };

        if (view == null)
            return padding;

        padding[0] = view.getPaddingLeft();
        padding[1] = view.getPaddingTop();
        padding[2] = view.getPaddingRight();
        padding[3] = view.getPaddingBottom();

        return padding;
    }

    /*
      set the padding of view from int array.
      */
    public static void setPaddingOfView(View view, int[] padding) {
        if (view != null)
            view.setPadding(padding[0], padding[1], padding[2], padding[3]);
    }

    /**
     *     set the background image, background color or font color.
     */
    private static void setViewBackground(View view, int type, int background) {
        int[] padding = getPaddingOfView(view);

        switch (type) {
            case EFFECT_IMAGE:
                if (view instanceof ImageView)
                    ((ImageView)view).setImageResource(background);
                else
                    view.setBackgroundResource(background);

                break;

            case EFFECT_COLOR:
                view.setBackgroundColor(background);
                break;

            case EFFECT_FONTCOLOR:
                TextView txtView = (TextView)view;
                txtView.setTextColor(background);
                break;

            case EFFECT_ALPHA:
                view.setAlpha(background / 100.0f);
                break;
        };

        setPaddingOfView(view, padding);
    }

    /**
     *  check a point that is in the view visible area
     */
    public static boolean checkLocalPointInView(View view, float x, float y) {
        int width = view.getWidth();
        int height = view.getHeight();
        return 0 < x && x < width && 0 < y && y < height;
    }
}
