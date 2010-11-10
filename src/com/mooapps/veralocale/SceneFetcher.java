package com.mooapps.veralocale;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.mooapps.veralocale.util.HttpHelper.RequestFailedException;

/**
 * Helper to fetch a list of scenes exposed by devices.
 */
public class SceneFetcher {

    public interface Callback {
        void onSceneFetcherResults(boolean success, List<Scene> scenes);
    }

    public static class Scene {
        public Scene(String name, int id, String serialNumber) {
            this.name = name;
            this.id = id;
            this.serialNumber = serialNumber;
        }

        public final String name;
        public final int id;
        public final String serialNumber;
    }

    private static List<Scene> cachedScenes;

    private final Context context;

    private final Callback callback;

    public SceneFetcher(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void fetch(List<String> serialNumbers) {
        final List<Scene> scenes = new ArrayList<Scene>();

        for (final String serialNumber : serialNumbers) {
            String url = Constants.getBaseRelativeUrl(context, serialNumber)
                    + "data_request?id=lu_sdata";
            ServerRequestDispatcher.FWD.request(url, true, true,
                    new ServerRequestDispatcher.Callback() {
                        public void onHttpSuccess(String payload) {
                            try {
                                fillScenes(payload, scenes, serialNumber);
                                callback.onSceneFetcherResults(true, scenes);
                            } catch (JSONException e) {
                                Log.e(Constants.TAG, "Could not get scenes", e);
                                callback.onSceneFetcherResults(false, null);
                                return;
                            }
                        }

                        public void onHttpFailure(RequestFailedException e) {
                            Log.e(Constants.TAG, "Could not get scenes", e);
                            callback.onSceneFetcherResults(false, null);
                        }
                    });
        }

    }

    public void fetchOrGiveCached(List<String> serialNumbers) {
        if (cachedScenes != null) {
            callback.onSceneFetcherResults(true, cachedScenes);
            return;
        }

        fetch(serialNumbers);
    }

    private void fillScenes(String payload, List<Scene> scenes,
            String serialNumber)
            throws JSONException {
        JSONObject results = new JSONObject(payload);
        JSONArray scenesJson = results.getJSONArray("scenes");
        for (int i = 0, n = scenesJson.length(); i < n; i++) {
            JSONObject scene = scenesJson.getJSONObject(i);
            scenes.add(new Scene(scene.getString("name"), scene.getInt("id"),
                    serialNumber));
        }
    }
}
