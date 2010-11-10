package com.mooapps.veralocale;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

/**
 * Activity which allows the user to enter his username and password.
 */
public class LoginCredentialsActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.login_credentials_activity);

        Preference validatePref = findPreference(Constants.PREF_KEY_VALIDATE);
        validatePref
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        validateCredentials();
                        return true;
                    }
                });
    }

    private void validateCredentials() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Validating...");
        progressDialog.show();

        new CredentialsValidator(this, new CredentialsValidator.Callback() {
            public void onCredentialsValidatorResults(boolean success) {
                progressDialog.dismiss();
                new AlertDialog.Builder(LoginCredentialsActivity.this)
                        .setMessage(
                                "Validation "
                                        + (success ? "succeeded" : "failed"))
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                    }
                                }).create().show();
            }
        }).validate();
    }
}
