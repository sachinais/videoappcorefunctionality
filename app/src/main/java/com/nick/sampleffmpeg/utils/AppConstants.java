package com.nick.sampleffmpeg.utils;

/**
 * Created by Vindhya Pratap on 9/17/2015.
 */
public class AppConstants {

    public static final String BASE_URL = "https://live.videomyjob.com/api/";
    public static final String SUCCESS = "success";
    public static final String MESSAGE = "message";
    public static final String APP_DOMAIN = "https://live.videomyjob.com/";
    public static final String FONT_SUFI_REGULAR = "SF-UI-Display-Regular.otf";
    public static final String FONT_SUFI_SEMIBOLD = "SF-UI-Display-Semibold.otf";
    public static final String FONT_AWESOME = "FontAwesome.otf";

    public static class FontMatch {
        public FontMatch(String fontTypeName, String fontFileName)
        {
            this.fontTypeName = fontTypeName;
            this.fontFileName = fontFileName;
        }
        public String fontTypeName;
        public String fontFileName;
    }

    public static final FontMatch[] CAPTION_FONTS = {
            new FontMatch("OpenSans", "OpenSans-Regular.ttf"),
            new FontMatch("DroidSans", "DroidSans.ttf"),
            new FontMatch("DroidSerif", "DroidSerif.ttf"),
            new FontMatch("sans-serif", "DroidSerif.ttf"),
            new FontMatch("ArchitectsDaughter", "ArchitectsDaughter.ttf"),
            new FontMatch("Arimo", "Arimo-Regular.ttf"),
            new FontMatch("Arvo", "Arvo-Regular.ttf"),
            new FontMatch("Bangers", "Bangers.ttf"),
            new FontMatch("Dosis", "Dosis-Regular.ttf"),
            new FontMatch("IndieFlower", "IndieFlower.ttf"),
            new FontMatch("JosefinSans", "JosefinSans-Regular.ttf"),
            new FontMatch("JosefinSlab", "JosefinSlab-Regular.ttf"),
            new FontMatch("KaushanScript", "KaushanScript-Regular.ttf"),
            new FontMatch("Lato", "Lato-Regular.ttf"),
            new FontMatch("LibreBaskerville", "LibreBaskerville-Regular.ttf"),
            new FontMatch("Lobster", "Lobster-Regular.ttf"),
            new FontMatch("Lora", "Lora-Regular.ttf"),
            new FontMatch("Merriweather", "Merriweather-Regular.ttf"),
            new FontMatch("Montserrat", "Montserrat-Regular.ttf"),
            new FontMatch("OldStandard", "OldStandard-Regular.ttf"),
            new FontMatch("Orbitron", "Orbitron-Regular.ttf"),
            new FontMatch("Oswald", "Oswald-Regular.ttf"),
            new FontMatch("PoiretOne", "PoiretOne-Regular.ttf"),
            new FontMatch("PT_Sans", "PT_Sans-Web-Regular.ttf"),
            new FontMatch("PT_Serif", "PT_Serif-Web-Regular.ttf"),
            new FontMatch("Quicksand", "Quicksand-Regular.ttf"),
            new FontMatch("Raleway", "Raleway-Regular.ttf"),
            new FontMatch("Roboto", "Roboto-Regular.ttf"),
            new FontMatch("SourceSansPro", "SourceSansPro-Regular.ttf"),
            new FontMatch("Ubuntu", "Ubuntu-Regular.ttf"),
            new FontMatch("VarelaRound", "VarelaRound-Regular.ttf"),
            new FontMatch("Vollkorn", "Vollkorn-Regular.ttf"),
    };

}
