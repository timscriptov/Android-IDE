package com.mcal.studio.helper;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;
import com.mcal.studio.R;
import com.mcal.studio.adapter.ProjectAdapter;
import com.mcal.studio.data.Preferences;
import com.mcal.studio.utils.FileUtils;
import com.mcal.studio.utils.Utils;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Locale;

/**
 * Helper class to handle all project related tasks
 */
public class ProjectManager {

    /**
     * Log TAG
     */
    private static final String TAG = ProjectManager.class.getSimpleName();

    /**
     * Method to handle project creation
     *
     * @param context        used to show toasts
     * @param appName        of project
     * @param appPackageName of project
     */
    public static void generate(Context context, String appName, String appPackageName, ProjectAdapter adapter, View view) {
        String nameNew = appName;
        int counter = 1;
        while (new File(Constants.PROJECT_ROOT + File.separator + nameNew).exists()) {
            nameNew = appName + "(" + counter + ")";
            counter++;
        }

        boolean status = false;
        status = generateDefault(context, nameNew, appPackageName);

        if (status) {
            adapter.insert(nameNew);
            Snackbar.make(view, R.string.project_success, Snackbar.LENGTH_SHORT).show();
        } else {
            Snackbar.make(view, R.string.project_fail, Snackbar.LENGTH_SHORT).show();
        }
    }

    private static boolean generateDefault(Context context, String appName, String packageName) {
        if (appName.isEmpty()) {
            Utils.toast(context, "Project appName empty");
        } else if (packageName.isEmpty()) {
            Utils.toast(context, "Package appName empty");
        } else if (!packageName.contains(".")) {
            Utils.toast(context, "Something is wrong!");
        } else if (com.mcal.studio.utils.FileUtils.isExistFile(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + "/" + appName)) {
            Utils.toast(context, "A project with same appName already exists");
        } else {
            // make res path
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/libs/");
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/assets/");
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/");
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/");
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/");
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/");
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/");
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/layout");
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values");
            // make java path
            String package_path = packageName.replace(".", "/") + File.separator;
            com.mcal.studio.utils.FileUtils.makeDir(com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/java/" + package_path);
            if (Preferences.getAndroidX()) {
                // copy res icons
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_hdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_mdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_xhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_xxhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_xxxhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_hdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_mdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_xhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_xxhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_xxxhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/" + "ic_launcher_round.png");
                // write files
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/build_root.gradle"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/build.gradle");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/settings.gradle"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/settings.gradle");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/proguard_rules.pro"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/proguard-rules.pro");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/build.gradle").replace("APPLICATION_ID", packageName), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/build.gradle");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/build.json").replace("APPLICATION_ID", packageName).replace("FIREBASE", String.valueOf(Preferences.getFirebase())).replace("ANDROIDX", String.valueOf(Preferences.getAndroidX())), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/build.json");
                // write xml files
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/AndroidManifest.xml").replace("PACKAGE", packageName), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/AndroidManifest.xml");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/activity_main.xml"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/layout/activity_main.xml");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/styles.xml"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/styles.xml");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/colors.xml"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/colors.xml");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/strings.xml").replace("APP_NAME", appName), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/strings.xml");
                // write java files
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/MainActivity.java").replace("PACKAGE", packageName), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/java/" + package_path + "MainActivity.java");
            } else {
                // copy res icons
                Utils.copyFileFromAssets("templates/android/ic_launcher_hdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_mdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_xhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_xxhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_xxxhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_hdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_mdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_xhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_xxhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_xxxhdpi.png", com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/" + "ic_launcher_round.png");
                // write files
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/android/build_root.gradle"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/build.gradle");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/android/settings.gradle"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/settings.gradle");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/android/proguard_rules.pro"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/proguard-rules.pro");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/android/build.gradle").replace("APPLICATION_ID", packageName), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/build.gradle");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/androidx/build.json").replace("APPLICATION_ID", packageName).replace("FIREBASE", String.valueOf(Preferences.getFirebase())).replace("ANDROIDX", String.valueOf(Preferences.getAndroidX())), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/build.json");
                // write xml files
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/android/AndroidManifest.xml").replace("PACKAGE", packageName), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/AndroidManifest.xml");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/android/activity_main.xml"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/layout/activity_main.xml");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/android/styles.xml"), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/styles.xml");
                com.mcal.studio.utils.FileUtils.writeFile2(Utils.readAssest("templates/android/strings.xml").replace("APP_NAME", appName), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/strings.xml");
                // write java files
                FileUtils.writeFile2(Utils.readAssest("templates/android/MainActivity.java").replace("PACKAGE", packageName), com.mcal.studio.data.Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/java/" + package_path + "MainActivity.java");
            }
            /*try {
                Android android = new Android("android-S", "30.0.3",
                        new DefaultConfig(packageName, "21", "30", "1", "1.0"),
                        new BuildTypes(true, false, false, false));
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();
                FileUtils.writeFile(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/build.json", gson.toJson(android));
            } catch (Exception e) {
                e.printStackTrace();
            }*/
        }
        return true;
    }

    public static void _import(String fileStr, String name, ProjectAdapter adapter, View view) {
        File file = new File(fileStr);
        String nameNew = name;
        int counter = 1;
        while (new File(Constants.PROJECT_ROOT + File.separator + nameNew).exists()) {
            nameNew = file.getName() + "(" + counter + ")";
            counter++;
        }

        File outFile = new File(Constants.PROJECT_ROOT + File.separator + nameNew);
        try {
            org.apache.commons.io.FileUtils.forceMkdir(outFile);
            org.apache.commons.io.FileUtils.copyDirectory(file, outFile);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            Snackbar.make(view, R.string.project_fail, Snackbar.LENGTH_SHORT).show();
            return;
        }

        adapter.insert(nameNew);
        Snackbar.make(view, R.string.project_success, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Check if project is valid
     *
     * @param string project title
     * @return true if project is valid
     */
    public static boolean isValid(String string) {
        return getIndexFile(string) != null;
    }

    /**
     * Method used to delete a project
     *
     * @param name of project
     */
    public static void deleteProject(String name) {
        File projectDir = new File(Constants.PROJECT_ROOT + File.separator + name);
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(projectDir);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
    }

    public static File getIndexFile(String project) {
        IOFileFilter filter = new NameFileFilter("build.json", IOCase.INSENSITIVE);
        Iterator<File> iterator = org.apache.commons.io.FileUtils.iterateFiles(new File(Constants.PROJECT_ROOT + File.separator + project + File.separator + "app"), filter, DirectoryFileFilter.DIRECTORY);
        return iterator.next();
    }

    /**
     * Guess whether given file is binary
     * Just checks for anything under 0x09
     */
    public static boolean isBinaryFile(File f) {
        int result = 0;
        try {
            FileInputStream in = new FileInputStream(f);
            int size = in.available();
            if (size > 1024) size = 1024;
            byte[] data = new byte[size];
            result = in.read(data);
            in.close();

            int ascii = 0;
            int other = 0;

            for (byte b : data) {
                if (b < 0x09) return true;

                if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D) ascii++;
                else if (b >= 0x20 && b <= 0x7E) ascii++;
                else other++;
            }

            return other != 0 && 100 * other / (ascii + other) > 95;

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + String.valueOf(result));
        }

        return true;
    }

    /**
     * Check if file is an image
     *
     * @param f file to check
     * @return true if file is an image
     */
    public static boolean isImageFile(File f) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(f.getAbsolutePath(), options);
        return options.outWidth != -1 && options.outHeight != -1;
    }

    public static boolean importFile(Context context, String name, Uri fileUri, String fileName) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            OutputStream outputStream = new FileOutputStream(Constants.PROJECT_ROOT + File.separator + name + File.separator + fileName);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream != null ? inputStream.read(buffer) : -1) != -1) {
                outputStream.write(buffer, 0, read);
            }
            if (inputStream != null) {
                inputStream.close();
            }
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }

        return true;
    }

    public static String humanReadableByteCount(long bytes) {
        int unit = 1000;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("kMGTPE").charAt(exp - 1) + "";
        return String.format(Locale.getDefault(), "%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
