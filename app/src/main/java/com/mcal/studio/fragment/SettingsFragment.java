package com.mcal.studio.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;

import com.mcal.studio.R;
import com.mcal.studio.data.NightMode;
import com.mcal.studio.data.Preferences;
import com.mcal.studio.helper.Constants;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import de.psdev.licensesdialog.LicensesDialog;

public class SettingsFragment extends PreferenceFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final SwitchPreference darkTheme = (SwitchPreference) getPreferenceManager().findPreference("dark_theme");
        darkTheme.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                NightMode.setMode(NightMode.getCurrentMode());
                restartPerfect(getActivity().getIntent());
                return true;
            }
        });

        final SwitchPreference darkThemeEditor = (SwitchPreference) getPreferenceManager().findPreference("dark_theme_editor");
        darkThemeEditor.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Preferences.setDarkThemeEditor((Boolean) o);
                return true;
            }
        });

        final SwitchPreference lineNumbers = (SwitchPreference) getPreferenceManager().findPreference("show_line_numbers");
        lineNumbers.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Preferences.setShowLineNumbers((Boolean) o);
                return true;
            }
        });

        final Preference factoryReset = getPreferenceManager().findPreference("factory_reset");
        String[] files = new File(Constants.PROJECT_ROOT).list();
        factoryReset.setEnabled(files != null && files.length > 0);
        factoryReset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Factory Reset")
                        .setMessage("Are you sure you want to delete ALL of your projects? This change cannot be undone!")
                        .setPositiveButton("RESET", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    FileUtils.cleanDirectory(new File(Constants.PROJECT_ROOT));
                                    factoryReset.setEnabled(false);
                                } catch (IOException e) {
                                    Log.e(TAG, e.toString());
                                }
                            }
                        })
                        .setNegativeButton("CANCEL", null)
                        .show();

                return true;
            }
        });

        Preference notices = getPreferenceManager().findPreference("notices");
        notices.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                LicensesDialog.Builder builder = new LicensesDialog.Builder(getActivity())
                        .setNotices(R.raw.notices);

                if (Preferences.isNightModeEnabled()) {
                    String formatString = getString(R.string.custom_notices_format_style);
                    String pBg = getRGBAString(0xff9e9e9e);
                    String bodyBg = getRGBAString(0xff424242);
                    String preBg = getRGBAString(0xffbdbdbd);
                    String liColor = "color: #ffffff";
                    String linkColor = "color: #1976D2";
                    String style = String.format(formatString, pBg, bodyBg, preBg, liColor, linkColor);
                    builder.setNoticesCssStyle(style);
                }

                builder.build().show();
                return true;
            }
        });

        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void restartPerfect(Intent intent) {
        getActivity().finish();
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        startActivity(intent);
    }

    private String getRGBAString(@ColorInt int color) {
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        float alpha = ((float) Color.alpha(color) / 255);
        return String.format(getString(R.string.rgba_background_format), red, green, blue, alpha);
    }
}
