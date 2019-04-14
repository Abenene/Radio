package org.oucho.radio2.tunein;

import org.oucho.radio2.radio.RadioKeys;

import java.io.InputStream;
import java.util.Scanner;

class TuneInCommon implements RadioKeys {


    static String getURLLink(String[] parts) {

        String url = null;

        for (String part : parts) {
            if (part.contains("URL=\""))
                url = part.replace("URL=\"", "").replace("\"", "");
        }

        return url;
    }


    static String getRadioURL(String[] parts) {

        String url = null;

        for (String part : parts) {
            if (part.contains("URL=\""))
                url = part.replace("URL=\"", "");
        }

        return url;
    }

    static String getRadioName(String[] parts) {
        String text = parts[1];

        return text.replace("text=\"", "");
    }


    static String convertStreamToString(InputStream is) {
        Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
