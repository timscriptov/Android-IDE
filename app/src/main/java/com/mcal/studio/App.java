package com.mcal.studio;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.mcal.studio.data.Preferences;

public class App extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;
    @SuppressLint("StaticFieldLeak")
    private static SharedPreferences mPreferences;

    public static Context getContext() {
        return context;
    }

    public static SharedPreferences getPreferences() {
        if (mPreferences == null) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        }
        return mPreferences;
    }

    @Override
    public void onCreate() {
        context = getApplicationContext();
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate();
        if (Preferences.isNightModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}