package me.austindizzy.wvuprtstatus.app;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.TwoStatePreference;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import android.text.format.DateFormat;

public class SettingsActivity extends AppCompatPreferenceActivity {
    public FirebaseAnalytics mFirebaseAnalytics;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new GeneralPreferenceFragment()).commit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            setHasOptionsMenu(true);

            final FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

            TwoStatePreference enableNotifs = ((TwoStatePreference)findPreference("enable_notifs"));
            findPreference("notif_types").setEnabled(enableNotifs.isChecked());
            findPreference("stations").setEnabled(enableNotifs.isChecked());
            enableNotifs.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    findPreference("notif_types").setEnabled(o.equals(true));
                    findPreference("stations").setEnabled(o.equals(true));
                    return true;
                }
            });

            Preference aboutBtn = findPreference("about");
            aboutBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {
                    mFirebaseAnalytics.setCurrentScreen(getActivity(), "About Screen", null);
                    buildAboutDialog(getActivity()).show();
                    return true;
                }
            });


        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        private final AlertDialog buildAboutDialog(Context context) {
            AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(context)
                    .setPositiveButton(getString(R.string.pref_okay), null)
                    .setNegativeButton(getString(R.string.pref_close), null);

            LayoutInflater inflater = LayoutInflater.from(context);
            View layout = inflater.inflate(R.layout.about_dialog, null);

            TextView aboutMsg = layout.findViewById(R.id.about_text);
            String msgText = getString(R.string.pref_about_text);
            msgText = msgText.replace("{{version}}", BuildConfig.VERSION_NAME)
                .replace("{{buildDate}}", DateFormat.format("M/d/y @ HH:m:s", BuildConfig.TIMESTAMP));
            aboutMsg.setText(Html.fromHtml(msgText));
            aboutMsg.setMovementMethod(LinkMovementMethod.getInstance());

            AlertDialog aboutDialog = aboutBuilder.create();
            aboutDialog.setView(layout, 0, 0, 0, 0);
            return aboutDialog;
        }
    }
}