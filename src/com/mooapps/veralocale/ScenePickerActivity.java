package com.mooapps.veralocale;

import java.util.List;
import java.util.WeakHashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuItem;

import com.mooapps.veralocale.SceneFetcher.Scene;

/**
 * Activity to allow the user to select a scene. This is a Locale edit activity,
 * so it will return the selected scene via the activity result.
 */
public class ScenePickerActivity extends PreferenceActivity {

    private Preference progressPreference;
    private WeakHashMap<Preference, Scene> prefScenes = new WeakHashMap<Preference, Scene>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefScreen = getPreferenceManager()
                .createPreferenceScreen(this);
        setPreferenceScreen(prefScreen);

        progressPreference = new Preference(this);
        progressPreference.setTitle("Refreshing scenes...");
        progressPreference.setEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!PreferenceManager.getDefaultSharedPreferences(this).contains(
                Constants.PREF_KEY_USERNAME)) {
            new AlertDialog.Builder(this).setTitle("Missing credentials")
                    .setMessage("Please enter your username and password")
                    .setPositiveButton(android.R.string.ok,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    startActivity(new Intent(
                                            ScenePickerActivity.this,
                                            LoginCredentialsActivity.class));
                                }
                            }).show();
        }

        refreshScenes(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Refresh").setIcon(R.drawable.ic_menu_refresh);
        menu.add("Cancel").setIcon(
                android.R.drawable.ic_menu_close_clear_cancel);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        CharSequence title = item.getTitle();
        if (title.equals("Refresh")) {
            refreshScenes(false);
        } else if (title.equals("Cancel")) {
            setResult(Activity.RESULT_CANCELED);
            finish();
        }

        return true;
    }

    private void refreshScenes(final boolean useCached) {
        PreferenceScreen prefScreen = getPreferenceScreen();

        prefScreen.removeAll();
        prefScreen.addPreference(progressPreference);

        prefScenes.clear();

        SerialNumberFetcher serialNumberFetcher = new SerialNumberFetcher(this,
                new SerialNumberFetcher.Callback() {
                    public void onSerialNumberFetcherResults(boolean success,
                            List<String> serialNumbers) {

                        if (!success) {
                            showErrorDialog();
                            return;
                        }

                        refreshScenesFromSerialNumbers(serialNumbers, useCached);
                    }
                });

        if (useCached) {
            serialNumberFetcher.fetchOrGiveCached();
        } else {
            serialNumberFetcher.fetch();
        }
    }

    private void refreshScenesFromSerialNumbers(List<String> serialNumbers,
            boolean useCached) {

        SceneFetcher sceneFetcher = new SceneFetcher(this,
                new SceneFetcher.Callback() {
                    public void onSceneFetcherResults(boolean success,
                            List<Scene> scenes) {

                        if (!success) {
                            showErrorDialog();
                            return;
                        }

                        PreferenceScreen prefScreen = getPreferenceScreen();

                        for (Scene scene : scenes) {
                            Preference pref = createPreferenceForScene(scene);
                            prefScreen.addPreference(pref);
                        }

                        prefScreen.removePreference(progressPreference);
                    }
                });

        if (useCached) {
            sceneFetcher.fetchOrGiveCached(serialNumbers);
        } else {
            sceneFetcher.fetch(serialNumbers);
        }
    }

    private Preference createPreferenceForScene(Scene scene) {
        Preference pref = new Preference(this);
        pref.setTitle(scene.name);
        prefScenes.put(pref, scene);

        return pref;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
            Preference scenePref) {

        Scene scene = prefScenes.get(scenePref);

        Bundle state = new Bundle();
        state.putInt(Constants.STATE_KEY_SCENE_ID, scene.id);
        state.putCharSequence(Constants.STATE_KEY_SERIAL_NUMBER,
                scene.serialNumber);

        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.LOCALE_KEY_BUNDLE, state);
        resultIntent.putExtra(Constants.LOCALE_KEY_BLURB, scenePref.getTitle());

        setResult(Activity.RESULT_OK, resultIntent);
        finish();

        return true;
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this).setTitle("Error").setMessage(
                "Could not get information from devices").setPositiveButton(
                android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).show();
    }
}
