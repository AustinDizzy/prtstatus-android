package me.austindizzy.wvuprtstatus.app;


import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.TwoStatePreference;
import android.provider.Settings;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
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
            TwoStatePreference enableWeather = ((TwoStatePreference)findPreference("enable_weather"));
            Preference notifCatStyleBtn = findPreference("oreo_cat");

            pollNotifEnabled(enableNotifs.isChecked());
            enableNotifs.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    pollNotifEnabled(o.equals(true));
                    return true;
                }
            });

            pollWeatherEnabled(enableWeather.isChecked());
            enableWeather.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    pollWeatherEnabled(o.equals(true));
                    return true;
                }
            });

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                ((PreferenceCategory) findPreference("notif_cat")).removePreference(notifCatStyleBtn);
            } else {
                notifCatStyleBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.O)
                    public boolean onPreferenceClick(Preference preference) {
                        Intent intent = new Intent()
                                .setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                                .putExtra(Settings.EXTRA_APP_PACKAGE, getContext().getPackageName())
                                .putExtra(Settings.EXTRA_CHANNEL_ID, R.string.notif_channel_name);
                        startActivity(intent);
                        return true;
                    }
                });
            }

            Preference clearBtn = findPreference("reset");
            clearBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(final Preference preference) {
                    new AlertDialog.Builder(preference.getContext())
                            .setTitle(getString(R.string.pref_confirm_title))
                            .setMessage(getString(R.string.pref_confirm_msg))
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    murderApplication(preference.getContext());
                                }
                            }).setNegativeButton(android.R.string.no, null).show();
                    return true;
                }
            });

            Preference aboutBtn = findPreference("about");
            aboutBtn.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference pref) {
                    mFirebaseAnalytics.setCurrentScreen(getActivity(), "About Screen", null);
                    showAboutDialog(getActivity());
                    return true;
                }
            });
        }

        private void murderApplication(Context context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ActivityManager manager = ((ActivityManager)context.getSystemService(ACTIVITY_SERVICE));
                if (manager != null) manager.clearApplicationUserData();
            } else {
                try {
                    Runtime.getRuntime().exec("pm clear " + context.getPackageName());
                } catch (Exception e) {
                    // TODO: something with e
                }
            }
        }

        private void pollWeatherEnabled(boolean b) {
            findPreference("temp_type").setEnabled(b);
        }

        private void pollNotifEnabled(boolean b) {
            String k[] = {"notif_types", "stations", "oreo_cat"};
            for (String s : k) findPreference(s).setEnabled(b);
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

        private void showAboutDialog(final Context context) {
            final AlertDialog.Builder aboutBuilder = new AlertDialog.Builder(context)
                    .setPositiveButton(getString(R.string.pref_okay), null)
                    .setNegativeButton(getString(R.string.pref_close), null);

            final PRTStatus lastStatus = AppDatabase.getAppDatabase(context).statusDao().getLast();
            final AlertDialog aboutDialog = aboutBuilder.create();

            View layout = View.inflate(context, R.layout.about_dialog, null);
            final ProgressBar bar = layout.findViewById(R.id.progress);
            final TextView aboutMsg = layout.findViewById(R.id.about_text);
            bar.setVisibility(View.VISIBLE);

            aboutDialog.setView(layout, 0, 0, 0, 0);
            aboutDialog.show();

            StringRequest request = new StringRequest(Request.Method.GET, getString(R.string.about_html), new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    response = response.replace("{{version}}", BuildConfig.VERSION_NAME)
                            .replace("{{buildDate}}", DateFormat.format("M/d/y @ HH:mm:ss", BuildConfig.TIMESTAMP))
                            .replace("{{lastStatusTimestamp}}", String.valueOf(lastStatus.getTimestamp()));

                    aboutMsg.setText(Html.fromHtml(response));
                    aboutMsg.setMovementMethod(LinkMovementMethod.getInstance());
                    bar.setVisibility(View.GONE);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //TODO: something with error
                }
            });

            HTTPRequestQueue.getInstance(context).addToRequestQueue(request);
        }
    }
}