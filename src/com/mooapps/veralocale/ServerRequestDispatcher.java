package com.mooapps.veralocale;

import android.os.AsyncTask;

import com.mooapps.veralocale.util.HttpHelper;
import com.mooapps.veralocale.util.HttpHelper.RequestFailedException;

/**
 * Helper to dispatch requests to the fwd{1,2} or sta{1,2}.mios.com servers.
 */
public enum ServerRequestDispatcher {
    STA(new String[] { "sta1.mios.com", "sta2.mios.com" }), FWD(new String[] {
            "fwd1.mios.com", "fwd2.mios.com" });

    public interface Callback {
        void onHttpSuccess(String payload);

        void onHttpFailure(RequestFailedException e);
    }

    private final String servers[];

    /**
     * The preferred server is usually the one that is known to be up.
     */
    private int preferredServerIndex = 0;

    private ServerRequestDispatcher(String[] servers) {
        this.servers = servers;
    }

    public void request(final String relativeUrl, final boolean secure,
            final boolean isEmptyPayloadAnError, final Callback callback) {

        // FIXME: Object return type needs to go
        new AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... params) {
                int initialServerIndex = preferredServerIndex, serverIndex = initialServerIndex;

                RequestFailedException initialException = null;
                do {
                    try {
                        String results = new HttpHelper()
                                .performGet(assembleUrl(relativeUrl, secure,
                                        serverIndex));

                        if (isEmptyPayloadAnError && results.length() == 0) {
                            throw new RequestFailedException(
                                    "Received empty response from server");
                        }

                        preferredServerIndex = serverIndex;
                        return results;

                    } catch (RequestFailedException e) {
                        if (initialException == null) {
                            initialException = e;
                        }
                    }

                    // Try next server
                    serverIndex = (serverIndex + 1) % servers.length;

                } while (serverIndex != initialServerIndex);

                // None of the servers worked
                return initialException;
            }

            @Override
            protected void onPostExecute(Object result) {
                if (result instanceof String) {
                    callback.onHttpSuccess((String) result);
                } else {
                    callback.onHttpFailure((RequestFailedException) result);
                }
            }
        }.execute((Void[]) null);
    }

    private String assembleUrl(String relativeUrl, boolean secure,
            int serverIndex) {
        return new StringBuilder(secure ? "https://" : "http://").append(
                servers[serverIndex]).append('/').append(relativeUrl)
                .toString();
    }
}
