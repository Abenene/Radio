package org.oucho.radio2.tunein;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.oucho.radio2.RadioApplication;
import org.oucho.radio2.radio.RadioKeys;
import org.oucho.radio2.utils.ImageFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Scanner;

import static org.oucho.radio2.utils.SendIntent.sendAddRadio;
import static org.oucho.radio2.utils.SendIntent.sendIntent;

public class TuneInCommon implements RadioKeys {


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


    static void saveRadio(String httpRequest, String name_radio, String url_image) {

        String url_radio = getURL(httpRequest);

        if (url_image == null)
            return;

        String img = getIMG(url_image);

        Log.d("TuneInCommon", "saveItem name_radio: " + name_radio + ", url: " + url_radio);

        String list[] = {INTENT_ADD_RADIO, url_radio, name_radio, img};
        sendAddRadio(RadioApplication.getInstance(), list);

    }


    private static String getURL(String httpRequest) {

        String[] url_radio = null;

        try {

            URL getUrl = new URL(httpRequest);
            HttpURLConnection connUrl = (HttpURLConnection) getUrl.openConnection();
            connUrl.setRequestProperty("User-Agent", USER_AGENT);
            connUrl.connect();
            InputStream streamUrl = connUrl.getInputStream();
            String url = convertStreamToString(streamUrl);
            streamUrl.close();

            url_radio = url.split("\n"); // a tendance à doubler l'url

        } catch (SocketTimeoutException e) {

            String list[] = {INTENT_ERROR, "error", "TimeoutException " + e};
            sendIntent(RadioApplication.getInstance(), list);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return url_radio[0];
    }


    private static String getIMG(String url_image) {

        String img = null;

        try {

            URL getImg = new URL(url_image);
            HttpURLConnection connImg = (HttpURLConnection) getImg.openConnection();
            connImg.setRequestProperty("User-Agent", USER_AGENT);
            connImg.connect();
            InputStream streamImg = connImg.getInputStream();
            Bitmap bmImg = BitmapFactory.decodeStream(streamImg);
            streamImg.close();

            img = ImageFactory.byteToString(ImageFactory.getBytes(ImageFactory.resize(bmImg)));

        } catch (SocketTimeoutException e) {

            String list[] = {INTENT_ERROR, "error", "TimeoutException " + e};
            sendIntent(RadioApplication.getInstance(), list);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return img;
    }



}
