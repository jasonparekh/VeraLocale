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
 * Helper to fetch relevant Vera serial numbers.
 */
public class SerialNumberFetcher implements ServerRequestDispatcher.Callback {

    public interface Callback {
        void onSerialNumberFetcherResults(boolean success,
                List<String> serialNumbers);
    }

    private static List<String> cachedSerialNumbers = null;

    private final Context context;
    private final Callback callback;
    private final String username;

    public SerialNumberFetcher(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;

        username = Constants.getUsername(context);
    }

    public void fetch() {
        final String username = Constants.getUsername(context);
        ServerRequestDispatcher.STA.request("locator_json.php?username="
                + username, false, false, this);
    }

    public void fetchOrGiveCached() {
        if (cachedSerialNumbers != null) {
            callback.onSerialNumberFetcherResults(true, cachedSerialNumbers);
        } else {
            fetch();
        }
    }

    public void onHttpFailure(RequestFailedException e) {
        Log
                .e(Constants.TAG,
                        "Could not make serial number fetching request", e);
        callback.onSerialNumberFetcherResults(false, null);
    }

    public void onHttpSuccess(String payload) {
        try {
            List<String> serialNumbers = new ArrayList<String>();

            JSONObject response = new JSONObject(payload);
            JSONArray units = response.getJSONArray("units");
            for (int unitsPos = 0, unitsLen = units.length(); unitsPos < unitsLen; unitsPos++) {
                JSONObject unit = units.getJSONObject(unitsPos);
                String serialNumber = unit.getString("serialNumber");
                JSONArray users = unit.getJSONArray("users");
                if (users.length() == 0) {
                    serialNumbers.add(serialNumber);
                } else {
                    for (int usersPos = 0, usersLen = users.length(); usersPos < usersLen; usersPos++) {
                        String curUser = users.getString(usersPos);
                        if (curUser.equalsIgnoreCase(username)) {
                            serialNumbers.add(serialNumber);
                        }
                    }
                }
            }

            callback.onSerialNumberFetcherResults(true, serialNumbers);
            cachedSerialNumbers = serialNumbers;
        } catch (JSONException e) {
            Log.e(Constants.TAG, "Could not fetch serial numbers", e);
            callback.onSerialNumberFetcherResults(false, null);
        }
    }

}
