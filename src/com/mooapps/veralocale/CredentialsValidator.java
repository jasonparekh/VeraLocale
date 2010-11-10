package com.mooapps.veralocale;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.mooapps.veralocale.util.HttpHelper.RequestFailedException;

/**
 * Helper to ensure the stored credentials are valid.
 */
public class CredentialsValidator {

    public interface Callback {
        void onCredentialsValidatorResults(boolean success);
    }

    private final Callback callback;
    private final Context context;

    public CredentialsValidator(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void validate() {
        new SerialNumberFetcher(context, new SerialNumberFetcher.Callback() {
            public void onSerialNumberFetcherResults(boolean success,
                    List<String> serialNumbers) {
                if (!success) {
                    callback.onCredentialsValidatorResults(false);
                    return;
                }

                if (serialNumbers.size() == 0) {
                    Log.e(Constants.TAG, "Did not get any serial numbers");
                    callback.onCredentialsValidatorResults(false);
                    return;
                }

                validate(serialNumbers.get(0));
            }
        }).fetch();
    }

    private void validate(final String serialNumber) {
        String relUrl = Constants.getBaseRelativeUrl(context, serialNumber)
                + "data_request?id=lu_alive";
        ServerRequestDispatcher.FWD.request(relUrl, true,
                false, new ServerRequestDispatcher.Callback() {
                    public void onHttpFailure(RequestFailedException e) {
                        Log.e(Constants.TAG, "Could not validate", e);
                        callback.onCredentialsValidatorResults(false);
                    }

                    public void onHttpSuccess(String payload) {
                        callback.onCredentialsValidatorResults(!payload
                                .startsWith("Invalid"));
                    }
                });
    }
}
