package com.mooapps.veralocale;

import android.content.Context;
import android.util.Log;

import com.mooapps.veralocale.util.HttpHelper.RequestFailedException;

/**
 * Helper to execute scenes.
 */
public class SceneRunner {

    private final Context context;
    private final Callback callback;

    public interface Callback {
        void onSceneRunnerResults(boolean success);
    }

    public SceneRunner(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void run(final String serialNumber, final int sceneId) {
        final String url = Constants.getBaseRelativeUrl(context, serialNumber)
                + "data_request?id=lu_action&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
                + sceneId;
        ServerRequestDispatcher.FWD.request(url, true, true,
                new ServerRequestDispatcher.Callback() {
                    public void onHttpSuccess(String payload) {
                        callback.onSceneRunnerResults(true);
                    }

                    public void onHttpFailure(RequestFailedException e) {
                        Log.e(Constants.TAG, "Could not run scene " + sceneId
                                + " on " + serialNumber + " via " + url, e);
                        callback.onSceneRunnerResults(false);
                    }
                });
    }
}
