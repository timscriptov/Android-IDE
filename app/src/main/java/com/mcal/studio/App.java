package com.mcal.studio;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.mcal.studio.utils.Prefs;

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
        new Prefs.Builder()
                .setContext(this)
                .setMode(ContextWrapper.MODE_PRIVATE)
                .setPrefsName(getPackageName())
                .setUseDefaultSharedPreference(true)
                .build();
        super.onCreate();
    }
}
