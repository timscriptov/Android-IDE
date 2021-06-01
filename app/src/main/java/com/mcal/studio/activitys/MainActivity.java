package com.mcal.studio.activitys;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.mcal.studio.data.Constants;
import com.mcal.studio.R;
import com.mcal.studio.builder.ApkMaker;
import com.mcal.studio.builder.BuildCallback;
import com.mcal.studio.data.Preferences;
import com.mcal.studio.utils.FileUtils;
import com.mcal.studio.utils.Prefs;
import com.mcal.studio.utils.Utils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mcal.studio.widget.CenteredToolbar;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private CenteredToolbar toolbar;
    private FloatingActionButton fab;
    private AppCompatEditText projectDir;
    private AppCompatImageButton compileBtn;
    private AppCompatTextView errors;
    private ProgressDialog progressDialog;
    private PowerManager powerManager;
    private PowerManager.WakeLock wakeLock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupToolbar(getString(R.string.app_name));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        } else {
            initialize();
        }

        fab = findViewById(R.id.fab);
        projectDir = findViewById(R.id.projectDir);
        compileBtn = findViewById(R.id.compile);
        errors = findViewById(R.id.logs);

        projectDir.setText(Prefs.getString("project", ""));

        fab.setOnClickListener(p1 -> showCreateProjDialog());

        compileBtn.setOnClickListener(p1 -> {
            File project = new File(projectDir.getText().toString());
            Prefs.putString("project", project.getAbsolutePath());
            if (project.exists() || project.isDirectory() || new File(project, "app/build.json").exists()) {
                ApkMaker maker = new ApkMaker(MainActivity.this);
                maker.setProjectDir(project.getAbsolutePath());
                maker.setBuildListener(new BuildCallback() {
                    @SuppressLint("InvalidWakeLockTag")
                    @Override
                    public void onStart() {
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setCancelable(false);
                        progressDialog.setMessage("Please wait...");
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                        progressDialog.show();
                        powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
                        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BUILDER");
                        wakeLock.acquire(10*60*1000L /*10 minutes*/);
                        errors.setText("No errors found");
                    }

                    @Override
                    public void onFailure(String message) {
                        progressDialog.dismiss();
                        wakeLock.release();
                        errors.setText(message);
                    }

                    @Override
                    public void onProgress(String progress) {
                        progressDialog.setMessage(progress);
                    }

                    @Override
                    public void onSuccess(File apk) {
                        progressDialog.dismiss();
                        wakeLock.release();
                        Utils.toast(getApplicationContext(), "Build Complete.");
                    }
                });
                maker.build();
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private void setupToolbar(String title) {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("WrongConstant")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.about) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("About");
            dialog.setItems(new String[]{"Telegram", "Gmail"}, (p112, p2) -> {
                switch (p2) {
                    case 0:
                        this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/dexprotect")));
                        break;
                    case 1:
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SENDTO);
                        intent.setData(Uri.parse("mailto:timscriptov@gmail.com"));
                        intent.putExtra(Intent.EXTRA_SUBJECT, this.getString(R.string.app_name));
                        try {
                            this.startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Snackbar.make(null, "Not Found Mail", Snackbar.LENGTH_SHORT).show();
                        }
                        break;
                }
            });
            dialog.create().show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void createProj(String appName, String packageName) {
        if (appName.isEmpty()) {
            Utils.toast(getApplicationContext(), "Project appName empty");
        } else if (packageName.isEmpty()) {
            Utils.toast(getApplicationContext(), "Package appName empty");
        } else if (!packageName.contains(".")) {
            Utils.toast(getApplicationContext(), "Something is wrong!");
        } else if (FileUtils.isExistFile(Constants.PROJECT_DIR.getAbsolutePath() + "/" + appName)) {
            Utils.toast(getApplicationContext(), "A project with same appName already exists");
        } else {
            // make res path
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/libs/");
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/assets/");
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/");
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/");
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/");
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/");
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/");
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/layout");
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values");
            // make java path
            String package_path = packageName.replace(".", "/") + File.separator;
            FileUtils.makeDir(Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/java/" + package_path);
            if(Preferences.getAndroidX()) {
                // copy res icons
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_hdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_mdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_xhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_xxhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_xxxhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_hdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_mdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_xhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_xxhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/androidx/ic_launcher_round_xxxhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/" + "ic_launcher_round.png");
                // write files
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/build_root.gradle"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/build.gradle");
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/settings.gradle"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/settings.gradle");
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/proguard_rules.pro"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/proguard-rules.pro");
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/build.gradle").replace("APPLICATION_ID", packageName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/build.gradle");
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/build.json").replace("APPLICATION_ID", packageName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/build.json");
                // write xml files
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/AndroidManifest.xml").replace("PACKAGE", packageName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/AndroidManifest.xml");
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/activity_main.xml"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/layout/activity_main.xml");
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/styles.xml"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/styles.xml");
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/colors.xml"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/colors.xml");
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/strings.xml").replace("APP_NAME", appName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/strings.xml");
                // write java files
                FileUtils.writeFile2(Utils.readAssest("templates/androidx/MainActivity.java").replace("PACKAGE", packageName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/java/" + package_path + "MainActivity.java");
            } else {
                // copy res icons
                Utils.copyFileFromAssets("templates/android/ic_launcher_hdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_mdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_xhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_xxhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_xxxhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/" + "ic_launcher.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_hdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-hdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_mdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-mdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_xhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xhdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_xxhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxhdpi/" + "ic_launcher_round.png");
                Utils.copyFileFromAssets("templates/android/ic_launcher_round_xxxhdpi.png", Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/mipmap-xxxhdpi/" + "ic_launcher_round.png");
                // write files
                FileUtils.writeFile2(Utils.readAssest("templates/android/build_root.gradle"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/build.gradle");
                FileUtils.writeFile2(Utils.readAssest("templates/android/settings.gradle"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/settings.gradle");
                FileUtils.writeFile2(Utils.readAssest("templates/android/proguard_rules.pro"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/proguard-rules.pro");
                FileUtils.writeFile2(Utils.readAssest("templates/android/build.gradle").replace("APPLICATION_ID", packageName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/build.gradle");
                FileUtils.writeFile2(Utils.readAssest("templates/android/build.json").replace("APPLICATION_ID", packageName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/build.json");
                // write xml files
                FileUtils.writeFile2(Utils.readAssest("templates/android/AndroidManifest.xml").replace("PACKAGE", packageName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/AndroidManifest.xml");
                FileUtils.writeFile2(Utils.readAssest("templates/android/activity_main.xml"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/layout/activity_main.xml");
                FileUtils.writeFile2(Utils.readAssest("templates/android/styles.xml"), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/styles.xml");
                FileUtils.writeFile2(Utils.readAssest("templates/android/strings.xml").replace("APP_NAME", appName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/res/values/strings.xml");
                // write java files
                FileUtils.writeFile2(Utils.readAssest("templates/android/MainActivity.java").replace("PACKAGE", packageName), Constants.PROJECT_DIR.getAbsolutePath() + File.separator + appName + "/app/src/main/java/" + package_path + "MainActivity.java");
            }
            // Message
            Utils.toast(getApplicationContext(), "Project Created");
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
    }

    private void initialize() {

    }

    private void showCreateProjDialog() {
        LinearLayout linearlayout = new LinearLayout(this);
        linearlayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearlayout.setOrientation(LinearLayout.VERTICAL);
        linearlayout.setPadding(25, 0, 25, 0);

        final AppCompatEditText appName = new AppCompatEditText(this);
        appName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        appName.setHint("Project Name");
        linearlayout.addView(appName);

        final AppCompatEditText packageName = new AppCompatEditText(this);
        packageName.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        packageName.setHint("Package Name");
        linearlayout.addView(packageName);

        final AppCompatCheckBox androidx = new AppCompatCheckBox(this);
        androidx.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        androidx.setText("AndroidX");
        linearlayout.addView(androidx);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Project");
        builder.setView(linearlayout);
        builder.setPositiveButton("Create", (p1, p2) -> {
            String name = appName.getText().toString().trim();
            String pack = packageName.getText().toString().trim();
            Preferences.setAndroidX(androidx.isChecked());
            createProj(name, pack);
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            initialize();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Do you want to exit this app?")
                .setPositiveButton("YES", (p1, p2) -> {
                    super.onBackPressed();
                    finish();
                })
                .setNegativeButton("NO", null)
                .create()
                .show();
    }

}
