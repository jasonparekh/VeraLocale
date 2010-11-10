package com.mooapps.veralocale;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Misc constants used by the app.
 */
public final class Constants {

    public static final String LOCALE_KEY_BLURB = "com.twofortyfouram.locale.intent.extra.BLURB";

    public static final String LOCALE_KEY_BUNDLE = "com.twofortyfouram.locale.intent.extra.BUNDLE";

    public static final String PREF_KEY_PASSWORD = "password";

    public static final String PREF_KEY_USERNAME = "username";

    public static final String PREF_KEY_VALIDATE = "validate";

    public static final String STATE_KEY_SCENE_ID = "scene";

    public static final String STATE_KEY_SERIAL_NUMBER = "serialNumber";

    public static final String TAG = "VeraLocale";

    /**
     * @return the base relative URL which includes the username, password, and
     *         serial number
     */
    public static String getBaseRelativeUrl(Context context, String serialNumber) {
        return new StringBuilder(getUsername(context)).append('/').append(
                getPassword(context)).append('/').append(serialNumber).append(
                '/').toString();
    }

    public static String getPassword(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_KEY_PASSWORD, "");
    }

    public static String getUsername(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getString(PREF_KEY_USERNAME, "");
    }

    private Constants() {
    }
}
