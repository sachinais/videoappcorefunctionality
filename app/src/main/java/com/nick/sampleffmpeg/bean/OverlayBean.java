package com.nick.sampleffmpeg.bean;

import android.graphics.Color;
import android.graphics.Point;
import android.util.Size;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by baebae on 1/8/16.
 */
public class OverlayBean {

    public class Overlay {
        private double width;
        private double height;
        private double x;
        private double y;
        private double marginLeft;
        private double marginTop;
        private double marginRight;
        private double marginBottom;
        private String backgroundImage = "";
        private int backgroundColor = Color.argb(0, 255, 255, 255);
        private int color = Color.argb(0, 255, 255, 255);
        private int fontSize;
        private String fontName;
    }

    public class BrandLogoOption {
        public boolean exist = false;
        public String topVideo = "";
        public String tailVideo = "";
    }
    public ArrayList<Overlay> captions = new ArrayList<>();
    public Overlay brandLogo = null;
    public BrandLogoOption brandLogoOption = null;
    public Overlay name = null;
    public Overlay contact = null;

    private double convertPercentToDouble(JSONObject obj, String field) throws Exception{
        String value = obj.getString(field);
        value = value.replace("%", "");
        return Double.parseDouble(value);
    }
    /**
     * @param obj overlay json object
     * @ret overlay object
     */
    private Overlay parseOverlay(JSONObject obj) throws Exception{
        Overlay overlay = new Overlay();
        Overlay ret = null;

        if (!obj.isNull("color")) {
            String color = obj.getString("color");
            overlay.color = Color.parseColor(color);
        }

        if (!obj.isNull("size")) {
            overlay.fontSize = obj.getInt("size");
        }

        if (!obj.isNull("font")) {
            overlay.fontName = obj.getString("font");
        }

        if (!obj.isNull("x")) {
            overlay.x = obj.getDouble("x");
        }

        if (!obj.isNull("y")) {
            overlay.y = obj.getDouble("y");
        }

        if (!obj.isNull("w")) {
            overlay.width = convertPercentToDouble(obj, "w");
        }
        if (!obj.isNull("h")) {
            overlay.height = convertPercentToDouble(obj, "h");
        }

        if (!obj.isNull("l")) {
            overlay.marginLeft = convertPercentToDouble(obj, "l");
        }

        if (!obj.isNull("t")) {
            overlay.marginTop = convertPercentToDouble(obj, "t");
        }

        if (!obj.isNull("r")) {
            overlay.marginRight = convertPercentToDouble(obj, "r");
        }

        if (!obj.isNull("b")) {
            overlay.marginBottom = convertPercentToDouble(obj, "b");
        }

        if (!obj.isNull("background-color")) {
            String color = obj.getString("background-color");
            color = color.substring(5, color.length() - 1);

            String colors[] = color.split(",");
            overlay.backgroundColor = Color.argb((int)(Double.parseDouble(colors[3]) * 255), Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
        }

        if (!obj.isNull("background-img")) {
            String backgroundImage = obj.getString("background-img");
            overlay.backgroundImage = backgroundImage.substring(backgroundImage.lastIndexOf("/")+1);
        }
        ret = overlay;
        return ret;
    }
    /**
     * parse template json in "data" value
     * @param obj json object
     * @throws Exception if exception is occured, it will throw automatically.
     */
    public void parseFromJson(JSONObject obj) throws Exception {
        captions = new ArrayList<>();
        brandLogo = null;
        name = null;
        contact = null;

        //parse brand logo
        if (!obj.isNull("brand-logo")) {
            JSONObject brandObj = obj.getJSONObject("brand-logo");
            brandLogo = parseOverlay(brandObj);
            brandLogoOption = new BrandLogoOption();
            if (!brandObj.isNull("exists")) {
                brandLogoOption.exist = brandObj.getBoolean("exists");
            }

            if (!brandObj.isNull("video_top")) {
                brandLogoOption.topVideo = brandObj.getString("video_top");
            }

            if (!brandObj.isNull("video_tail")) {
                brandLogoOption.tailVideo = brandObj.getString("video_tail");
            }
        }

        //parse name overlay
        if (!obj.isNull("name-overlay")) {
            name = parseOverlay(obj.getJSONObject("brand-logo"));
        }

        //parse contact overlay
        if (!obj.isNull("contact-overlay")) {
            contact = parseOverlay(obj.getJSONObject("contact-overlay"));
        }

        if (!obj.isNull("captions")) {
            JSONArray captionJsonArray = obj.getJSONArray("captions");
            for (int i = 0; i < captionJsonArray.length(); i ++) {
                Overlay caption = parseOverlay(captionJsonArray.getJSONObject(i));
                captions.add(caption);
            }
        }
    }

}
