package org.oucho.radio2.tunein;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import org.oucho.radio2.RadioApplication;
import org.oucho.radio2.radio.RadioKeys;
import org.oucho.radio2.utils.ImageFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import static org.oucho.radio2.tunein.TuneInCommon.convertStreamToString;
import static org.oucho.radio2.utils.SendIntent.sendAddRadio;
import static org.oucho.radio2.utils.SendIntent.sendIntent;

class SaveRadio implements RadioKeys {


    static void addRadio(String item) {

        String[] parts = item.split("\" ");

        if  (item.contains("type=\"audio\"")) {

            String text = parts[1];
            String name = text.replace("text=\"" , "");
            String url = null;
            String url_image = null;

            Log.d("SaveRadio", "name: " + name);

            for (String part : parts) {

                if (part.contains("URL=\"")) {
                    url = part.replace("URL=\"", "");
                }

                if (part.contains("image=\"")) {
                    url_image = part.replace("image=\"", "");
                }
            }

            new saveItem(url, name, url_image).execute();
        }
    }


    private static class saveItem extends AsyncTask<Void, Void, Void> {

        final String httpRequest;
        final String name_radio;
        final String url_image;

        saveItem(String url, String name, String img){

            this.httpRequest = url;
            this.name_radio = name;
            this.url_image = img;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            saveRadio(httpRequest, name_radio, url_image);

            return null;
        }
    }


    private static void saveRadio(String httpRequest, String name_radio, String url_image) {

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

        return url_radio != null ? url_radio[0] : null;
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
