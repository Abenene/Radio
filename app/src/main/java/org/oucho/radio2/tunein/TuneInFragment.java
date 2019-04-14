/*
 * Radio - Internet radio for android
 * Copyright (C) 2017  Old-Geek
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.oucho.radio2.tunein;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import org.oucho.radio2.R;
import org.oucho.radio2.RadioApplication;
import org.oucho.radio2.radio.RadioKeys;
import org.oucho.radio2.radio.RadioService;
import org.oucho.radio2.tunein.adapters.BaseAdapter;
import org.oucho.radio2.tunein.adapters.TuneInAdapter;
import org.oucho.radio2.tunein.loaders.TuneInLoader;
import org.oucho.radio2.utils.State;
import org.oucho.radio2.view.CustomLayoutManager;
import org.oucho.radio2.view.fastscroll.FastScrollRecyclerView;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import static org.oucho.radio2.tunein.TuneInCommon.convertStreamToString;
import static org.oucho.radio2.tunein.TuneInCommon.getRadioName;
import static org.oucho.radio2.tunein.TuneInCommon.getRadioURL;
import static org.oucho.radio2.tunein.TuneInCommon.getURLLink;
import static org.oucho.radio2.tunein.TuneInCommon.saveRadio;
import static org.oucho.radio2.utils.SendIntent.sendActionIntent;
import static org.oucho.radio2.utils.SendIntent.sendIntent;


public class TuneInFragment extends Fragment implements RadioKeys {

    private List<String> historique = new ArrayList<>();
    private static final String TAG = "TuneInFragment";
    private TuneInAdapter mAdapter;
    private Context mContext;
    private LinearLayout progressBar;

    private Receiver receiver;
    private FastScrollRecyclerView mRecyclerView;

    public TuneInFragment() {
    }


    public static TuneInFragment newInstance() {
        return new TuneInFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_tunein, container, false);

        mRecyclerView = rootView.findViewById(R.id.recyclerview);
        mRecyclerView.setLayoutManager(new CustomLayoutManager(mContext));
        mAdapter = new TuneInAdapter();
        mAdapter.setOnItemClickListener(mOnItemClickListener);
        mRecyclerView.setAdapter(mAdapter);

        progressBar = rootView.findViewById(R.id.progressBar_layout);

        Bundle args = new Bundle();
        args.putString("url", HOME);

        load(args);

        return rootView;
    }


    private void load(Bundle args) {
        Animation animFadeIn = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
        animFadeIn.setDuration(200);
        progressBar.setAnimation(animFadeIn);
        progressBar.setVisibility(View.VISIBLE);
        getLoaderManager().restartLoader(0, args, mLoaderCallbacks);
    }

    private final LoaderManager.LoaderCallbacks<List<String>> mLoaderCallbacks = new LoaderManager.LoaderCallbacks<List<String>>() {

        @Override
        public Loader<List<String>> onCreateLoader(int id, Bundle args) {

            String url = args.getString("url");
            historique.add(url);

            if (historique.size() > 1) {
                setHomeButton(true);
            } else {
                setHomeButton(false);
            }
            return new TuneInLoader(mContext, args.getString("url"));
        }

        @Override
        public void onLoadFinished(Loader<List<String>> loader, List<String> list) {
            if (list != null)
                mAdapter.setData(list);

            Animation animFadeOut = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_out);
            animFadeOut.setDuration(200);
            progressBar.setAnimation(animFadeOut);
            progressBar.setVisibility(View.GONE);

            mRecyclerView.scrollToPosition(0);
        }

        @Override
        public void onLoaderReset(Loader<List<String>> loader) {}
    };


    private final BaseAdapter.OnItemClickListener mOnItemClickListener = new BaseAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(int position, View v) {

            String item = mAdapter.getItem(position);
            String[] parts = item.split("\" ");

            if (v.getId() == R.id.radio_ajout) {

                add(item);

            } else {

                if (item.contains("type=\"link\"")) {

                    Bundle args = new Bundle();
                    args.putString("url", getURLLink(parts));

                    load(args);
                }

                if (item.contains("type=\"audio\"")) {
                    new TunInPlaying.playItem(getRadioURL(parts), getRadioName(parts)).execute();
                }
            }
        }
    };


    private void add(String item) {

        String[] parts = item.split("\" ");

        if  (item.contains("type=\"audio\"")) {

            String text = parts[1];
            String name = text.replace("text=\"" , "");
            String url = null;
            String url_image = null;

            Log.d(TAG, "name: " + name);

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


    @Override
    public void onResume() {
        super.onResume();

        receiver = new Receiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(INTENT_SEARCH);
        filter.addAction(INTENT_FOCUS);
        filter.addAction(INTENT_HOME);

        mContext.registerReceiver(receiver, filter);

        // Active la touche back
        //noinspection ConstantConditions
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();
        getView().setOnKeyListener((v, keyCode, event) -> {

            AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                //noinspection ConstantConditions
                audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
            }

            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                //noinspection ConstantConditions
                audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);
            }

            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {

                if (historique.size() > 1) {

                    String last = historique.get(historique.size() -2);

                    Bundle args = new Bundle();
                    args.putString("url", last);

                    // supprime les 2 derniers.
                    historique.remove(historique.size() - 1);
                    historique.remove(historique.size() - 1);

                    if (historique.size() > 1)
                        setHomeButton(true);
                    else
                        setHomeButton(false);

                    load(args);

                    return true;

                } else {

                    goRadioList();
                }

            }
            return true;
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mContext.unregisterReceiver(receiver);
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String receiveIntent = intent.getAction();

            if (INTENT_SEARCH.equals(receiveIntent)) {

                String text = intent.getStringExtra("search");
                search(text);
            }

            if (INTENT_HOME.equals(receiveIntent) ) {

                try {
                    if (intent.getStringExtra("go").equals("home")) {
                        historique = new ArrayList<>();

                        Bundle args = new Bundle();
                        args.putString("url", HOME);
                        setHomeButton(false);

                        load(args);
                    }

                    if (intent.getStringExtra("go").equals("back"))
                        goRadioList();

                } catch (RuntimeException ignore) {}


            }

            if (INTENT_FOCUS.equals(receiveIntent)) {
                //noinspection ConstantConditions
                getView().setFocusableInTouchMode(true);
                getView().requestFocus();
            }

        }
    }

    private void setHomeButton(Boolean value) {
        String list[] = {INTENT_HOME, "setButton"};
        sendIntent(mContext, list, value);
    }

    private void goRadioList() {
        String list[] = {INTENT_TITRE, "titre", getResources().getString(R.string.app_name)};

        sendIntent(mContext, list);

        setHomeButton(false);

        FragmentTransaction ft = Objects.requireNonNull(getFragmentManager()).beginTransaction();
        ft.setCustomAnimations(R.anim.slide_out_bottom, R.anim.slide_out_bottom);
        ft.remove(TuneInFragment.this);
        ft.commit();
    }

    private void search(String search) {

        String query = search.replace(" ", "%20");

        Bundle args = new Bundle();
        args.putString("url", HOME + "/Search.ashx?query=" + query);

        //noinspection ConstantConditions
        getView().setFocusableInTouchMode(true);
        getView().requestFocus();

        load(args);
    }

}
