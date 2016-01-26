package com.nick.sampleffmpeg.bean;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Picture;
import android.graphics.Point;
import android.graphics.drawable.PictureDrawable;
import android.util.Size;

import com.nick.sampleffmpeg.Define.Constant;
import com.nick.sampleffmpeg.sharedpreference.SPreferenceKey;
import com.nick.sampleffmpeg.sharedpreference.SharedPreferenceWriter;
import com.nick.sampleffmpeg.utils.AppConstants;
import com.nick.sampleffmpeg.utils.FileUtils;
import com.nick.sampleffmpeg.utils.svgandroid.SVG;
import com.nick.sampleffmpeg.utils.svgandroid.SVGParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by baebae on 1/8/16.
 */
public class OverlayBean {

    public class Overlay {
        public String defaultText = "";

        public double width;
        public double height;
        public double x;
        public double y;
        public double marginLeft;
        public double marginTop;
        public double marginRight;
        public double marginBottom;
        public String backgroundImage = "";
        public int backgroundColor = Color.argb(0, 255, 255, 255);
        public int color = Color.argb(0, 255, 255, 255);
        public int fontSize;
        public String fontName;

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

    public String strDirectoryID = "";
    public JSONObject debugJsonObj = null;

    private SharedPreferenceWriter sharedPreferenceWriter = null;

    private double convertPercentToDouble(JSONObject obj, String field) throws Exception{
        String value = obj.getString(field);
        value = value.replace("%", "");
        return Double.parseDouble(value);
    }

    //Convert PictureDrawable to Bitmap
    private static Bitmap picture2Bitmap(Picture picture){
        Bitmap bitmap = Bitmap.createBitmap(picture.getWidth(), picture.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawPicture(picture);
        return bitmap;
    }

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    private String getFontFileName(String fontName) {
        if (fontName.contains("OpenSans")) {
            return AppConstants.FONT_OPEN_SANS;
        }

        if (fontName.contains("sans-serif")) {
            return AppConstants.FONT_SANS_SERIF;
        }

        return  "";
    }
    /**
     * @param obj overlay json object
     * @ret overlay object
     */
    private Overlay parseOverlay(JSONObject obj, boolean brand) throws Exception{
        Overlay overlay = new Overlay();
        Overlay ret = null;
        if (!obj.isNull("color")) {
            String color = obj.getString("color");
            if (color.length() == 4) {
                color = "#" + color.charAt(1) + color.charAt(1) + color.charAt(2) + color.charAt(2) + color.charAt(3) + color.charAt(3);
            }
            overlay.color = Color.parseColor(color);
        }

        if (!obj.isNull("size")) {
            overlay.fontSize = obj.getInt("size");
        }

        if (!obj.isNull("font")) {
            String fontName = obj.getString("font");
            overlay.fontName = getFontFileName(fontName);
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
            for (int i = 0; i < colors.length; i ++) {
                colors[i] = colors[i].replace(" ", "");
            }
            overlay.backgroundColor = Color.argb((int)(Double.parseDouble(colors[3]) * 255), Integer.parseInt(colors[0]), Integer.parseInt(colors[1]), Integer.parseInt(colors[2]));
        }

        if (brand) {
            overlay.backgroundImage = "brand.png";
        }
        if (!obj.isNull("background-img")) {
            String backgroundImage = obj.getString("background-img");
            overlay.backgroundImage = backgroundImage.substring(backgroundImage.lastIndexOf("/") + 1);
            if (overlay.backgroundImage.contains("svg")) {
                String fileName = Constant.getApplicationDirectory() + strDirectoryID + File.separator + overlay.backgroundImage;
                File file = new File(fileName);
                FileInputStream fileInputStream = new FileInputStream(file);
                String svgContent = convertStreamToString(fileInputStream);
                String changeColor = "fill=\"#" + Integer.toHexString(overlay.backgroundColor ) + "\"";
                svgContent = svgContent.replace("fill=\"inherit\"", changeColor);
                SVG svg = SVGParser.getSVGFromString(svgContent);

                Bitmap bitmap = picture2Bitmap(svg.getPicture());
                overlay.backgroundImage = overlay.backgroundImage.replace(".svg", ".png");
                fileName = Constant.getApplicationDirectory() + strDirectoryID + File.separator + overlay.backgroundImage;
                bitmap.compress(Bitmap.CompressFormat.PNG, 100,  new FileOutputStream(fileName));
            }
        }

        ret = overlay;
        return ret;
    }
    /**
     * parse template json in "data" value
     * @param obj json object
     * @throws Exception if exception is occured, it will throw automatically.
     */
    public void parseFromJson(JSONObject obj, String directoryID) throws Exception {
        captions = new ArrayList<>();
        brandLogo = null;
        name = null;
        contact = null;

        debugJsonObj = obj;
        strDirectoryID = directoryID;

        sharedPreferenceWriter = SharedPreferenceWriter.getInstance();
        //parse brand logo
        if (!obj.isNull("brand-logo")) {
            JSONObject brandObj = obj.getJSONObject("brand-logo");
            brandLogo = parseOverlay(brandObj, true);
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
            name = parseOverlay(obj.getJSONObject("name-overlay"), false);
            if (sharedPreferenceWriter != null) {
                String strName = sharedPreferenceWriter.getString(SPreferenceKey.FIRST_NAME) + " " + sharedPreferenceWriter.getString(SPreferenceKey.LAST_NAME);
                name.defaultText = strName + "\n" + sharedPreferenceWriter.getString(SPreferenceKey.TITLE);
            }
        }

        //parse contact overlay
        if (!obj.isNull("contact-overlay")) {
            contact = parseOverlay(obj.getJSONObject("contact-overlay"), false);
            if (sharedPreferenceWriter != null) {
                contact.defaultText = sharedPreferenceWriter.getString(SPreferenceKey.EMAIL) + "\n" + sharedPreferenceWriter.getString(SPreferenceKey.CONTACT);
            }
        }

        if (!obj.isNull("captions")) {
            JSONArray captionJsonArray = obj.getJSONArray("captions");
            for (int i = 0; i < captionJsonArray.length(); i ++) {
                Overlay caption = parseOverlay(captionJsonArray.getJSONObject(i), false);
                captions.add(caption);
            }
        }
    }

}
