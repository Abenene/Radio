package org.oucho.radio2.tunein;

public class TuneInCommon {

    public static String getURLLink(String[] parts) {


        String url = null;

        for (String part : parts) {

            if (part.contains("URL=\"")) {
                url = part.replace("URL=\"", "").replace("\"", "");
            }
        }

        return url;
    }


    public static String getRadioURL(String[] parts) {

        String url = null;

        for (String part : parts) {
            if (part.contains("URL=\"")) {
                url = part.replace("URL=\"", "");
            }
        }

        return url;
    }

    public static String getRadioName(String[] parts) {
        String text = parts[1];

        return text.replace("text=\"", "");

    }


}
