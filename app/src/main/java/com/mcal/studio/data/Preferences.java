package com.mcal.studio.data;

import com.mcal.studio.App;

public class Preferences {
    public static String getRemote() {
        return App.getPreferences().getString("remote", "");
    }

    public static void setRemote(String flag) {
        App.getPreferences().edit().putString("remote", flag).apply();
    }

    public static String getCloneName() {
        return App.getPreferences().getString("clone_name", "");
    }

    public static void setCloneName(String flag) {
        App.getPreferences().edit().putString("clone_name", flag).apply();
    }

    public static void setStoreProject(String name, String flag) {
        App.getPreferences().edit().putString(name, flag).apply();
    }

    public static String getAppPackageName() {
        return App.getPreferences().getString("appPackageName", "");
    }

    public static void setAppPackageName(String flag) {
        App.getPreferences().edit().putString("appPackageName", flag).apply();
    }

    public static String getAppName() {
        return App.getPreferences().getString("appName", "");
    }

    public static void setAppName(String flag) {
        App.getPreferences().edit().putString("appName", flag).apply();
    }

    public static boolean getShowLineNumbers() {
        return App.getPreferences().getBoolean("show_line_numbers", false);
    }

    public static void setShowLineNumbers(boolean flag) {
        App.getPreferences().edit().putBoolean("show_line_numbers", flag).apply();
    }

    public static boolean getDarkThemeEditor() {
        return App.getPreferences().getBoolean("dark_theme_editor", false);
    }

    public static void setDarkThemeEditor(boolean flag) {
        App.getPreferences().edit().putBoolean("dark_theme_editor", flag).apply();
    }

    public static boolean isIntro() {
        return App.getPreferences().getBoolean("intro_done", false);
    }

    public static void setIntro(boolean flag) {
        App.getPreferences().edit().putBoolean("intro_done", flag).apply();
    }

    public static boolean isWordwrap() {
        return App.getPreferences().getBoolean("wordwrap", false);
    }

    public static boolean isNightModeEnabled() {
        return App.getPreferences().getBoolean("dark_theme", false);
    }

    public static void setNightModeEnabled(boolean flag) {
        App.getPreferences().edit().putBoolean("dark_theme", flag).apply();
    }

    public static boolean getAndroidX() {
        return App.getPreferences().getBoolean("AndroidX", false);
    }

    public static void setAndroidX(boolean flag) {
        App.getPreferences().edit().putBoolean("AndroidX", flag).apply();
    }

    public static boolean getFirebase() {
        return App.getPreferences().getBoolean("Firebase", false);
    }

    public static void setFirebase(boolean flag) {
        App.getPreferences().edit().putBoolean("Firebase", flag).apply();
    }
}
