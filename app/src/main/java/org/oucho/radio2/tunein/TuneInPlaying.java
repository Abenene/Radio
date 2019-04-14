package org.oucho.radio2.tunein;

import android.os.AsyncTask;
import android.util.Log;

import org.oucho.radio2.RadioApplication;
import org.oucho.radio2.radio.RadioKeys;
import org.oucho.radio2.radio.RadioService;
import org.oucho.radio2.utils.State;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import static org.oucho.radio2.tunein.TuneInCommon.convertStreamToString;
import static org.oucho.radio2.utils.SendIntent.sendActionIntent;
import static org.oucho.radio2.utils.SendIntent.sendIntent;

class TuneInPlaying implements RadioKeys {


    //                                   <Params, Progress, Result>
    static class playItem extends AsyncTask<Object, Void, String> {

        final String url;
        final String name;

        playItem(String url, String name) {
            this.url = url;
            this.name = name;
        }

        protected String doInBackground(Object... objects) {

            return play(url, name);
        }
    }


    private static String play(String url, String name) {

        String finalURL = getURL(url);

        if (finalURL == null)
            return "not good";

        if ( !(State.isPlaying() && finalURL.equals(RadioService.getUrl())) ) {

            String list[] = {ACTION_PLAY, finalURL, name};
            sendActionIntent(RadioApplication.getInstance(), list);
        }

        return "ok";

    }


    private static String getURL(String url) {

        url = url.replace(" ", "%20");

        String[] finalURL = null;

        try {

            URL getUrl = new URL(url);
            HttpURLConnection connUrl = (HttpURLConnection) getUrl.openConnection();
            connUrl.setRequestProperty("User-Agent", USER_AGENT);
            connUrl.connect();
            InputStream streamUrl = connUrl.getInputStream();
            String data = convertStreamToString(streamUrl);
            streamUrl.close();

            Log.d("TuneInPlaying", "playItem url: " + data);

            finalURL = data.split("\n"); // a tendance à doubler l'url


        } catch (SocketTimeoutException e) {

            String list[] = {INTENT_ERROR, "error", "TimeoutException " + e};
            sendIntent(RadioApplication.getInstance(), list);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return finalURL != null ? finalURL[0] : null;

    }
}
