package com.mcal.studio.data;

import com.mcal.studio.App;

public class Preferences {
    public static boolean getAndroidX() {
        return App.getPreferences().getBoolean("AndroidX", false);
    }

    public static void setAndroidX(boolean flag) {
        App.getPreferences().edit().putBoolean("AndroidX", flag).apply();
    }
}
