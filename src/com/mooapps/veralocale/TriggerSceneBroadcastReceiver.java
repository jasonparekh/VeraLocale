package com.mooapps.veralocale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Receiver that executes signals upon receiving a broadcast from Locale.
 */
public class TriggerSceneBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        final int sceneId = extras.getInt(Constants.STATE_KEY_SCENE_ID, 0);
        final String serialNumber = extras
                .getString(Constants.STATE_KEY_SERIAL_NUMBER);

        final SceneRunner sceneRunner = new SceneRunner(context,
                new SceneRunner.Callback() {
                    public void onSceneRunnerResults(boolean success) {
                        if (!success) {
                            Log.e(Constants.TAG, "Could not run scene");
                        }
                    }
                });

        sceneRunner.run(serialNumber, sceneId);
    }

}
