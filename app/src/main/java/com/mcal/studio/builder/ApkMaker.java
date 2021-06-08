package com.mcal.studio.builder;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import androidx.appcompat.app.AlertDialog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mcal.studio.data.gson.build.Android;
import com.mcal.studio.data.gson.firebase.Firebase;
import com.mcal.studio.utils.AbiInfo;
import com.mcal.studio.utils.FileUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ApkMaker extends AsyncTask<String, String, String> {
    @SuppressLint("StaticFieldLeak")
    private final Context context;
    private final List<File> allLibs = new ArrayList<>();
    private BuildCallback callback;
    private String packageName, versionName, versionCode, minSdkVersion, targetSdkVersion = null;
    private boolean isAndroidX, isDebug, isMinify, isJava8 = false;
    private File project;
    private File bin;
    private File androidxLibs;
    private File libs;
    private File aarLibs;
    private File sdk;
    private File keys;
    private File jars;
    private File buildBin;
    private File release;
    private File jniLibs;
    private File gen;
    private File genFirebase;
    private File classes;
    private File dexes;
    private File temp;
    private String defaultWebClientId;
    private String firebaseDatabaseUrl;
    private String gcmDefaultSenderId;
    private String googleApiKey;
    private String googleAppId;
    private String googleCrashReportingApiKey;
    private String googleStorageBucket;
    private String projectId;

    // AIDL
    //C:\Android-project\ApiDemos\src\com\example\android\apis\app>aidl -IC:\Android-project\ApiDemos\src\ IRemoteService.aidl

    public ApkMaker(Context context) {
        this.context = context;
    }

    public void setProjectDir(String str) {
        this.project = new File(str);
    }

    public void setBuildListener(BuildCallback callback) {
        this.callback = callback;
    }

    public void build() {
        bin = new File(context.getFilesDir(), "bin");
        androidxLibs = new File(context.getFilesDir(), "libs");
        sdk = new File(context.getFilesDir(), "sdk");
        keys = new File(context.getFilesDir(), "key");
        jars = new File(context.getFilesDir(), "jars");
        buildBin = new File(project, "app/build/bin");
        release = new File(project, "app/release");
        libs = new File(project, "app/libs");
        aarLibs = new File(project, "app/build/libs");
        jniLibs = new File(project, "app/src/main/lib"); // Shared libraries
        gen = new File(project, "app/build/gen");
        genFirebase = new File(project, "app/build/res");
        classes = new File(project, "app/build/bin/classes");
        dexes = new File(project, "app/build/bin/dexes");
        temp = new File(project, "app/build/bin/temp");
        this.execute();
    }

    @Override
    protected void onPreExecute() {
        callback.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String[] p1) {

        try {
            FileUtils.deleteFile(buildBin.getAbsolutePath());
            if (!bin.exists()) bin.mkdirs();
            if (!androidxLibs.exists()) androidxLibs.mkdirs();
            if (!sdk.exists()) sdk.mkdirs();
            if (!keys.exists()) keys.mkdirs();
            if (!jars.exists()) jars.mkdirs();
            if (!buildBin.exists()) buildBin.mkdirs();
            if (!release.exists()) release.mkdirs();
            if (!libs.exists()) libs.mkdirs();
            if (!aarLibs.exists()) aarLibs.mkdirs();
            if (!gen.exists()) gen.mkdirs();
            if (!genFirebase.exists()) genFirebase.mkdirs();
            if (!classes.exists()) classes.mkdirs();
            if (!dexes.exists()) dexes.mkdirs();
            if (!temp.exists()) temp.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!new File(bin, "aapt").exists()) {
            try {
                InputStream input = context.getAssets().open("bin/" + AbiInfo.getBinaryName("aapt"));
                OutputStream output = new FileOutputStream(new File(bin, "aapt"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
                new File(bin, "aapt").setExecutable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!new File(bin, "aapt2").exists()) {
            try {
                InputStream input = context.getAssets().open("bin/" + AbiInfo.getBinaryName("aapt2"));
                OutputStream output = new FileOutputStream(new File(bin, "aapt2"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
                new File(bin, "aapt2").setExecutable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!new File(bin, "zipalign").exists()) {
            try {
                InputStream input = context.getAssets().open("bin/" + AbiInfo.getBinaryName("zipalign"));
                OutputStream output = new FileOutputStream(new File(bin, "zipalign"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
                new File(bin, "zipalign").setExecutable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!new File(context.getFilesDir(), "proguard.txt").exists()) {
            try {
                InputStream input = context.getAssets().open("proguard.txt");
                OutputStream output = new FileOutputStream(new File(context.getFilesDir(), "proguard.txt"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (sdk.listFiles().length == 0) {
            try {
                InputStream input = context.getAssets().open("libs/sdk.zip");
                OutputStream output = new FileOutputStream(new File(context.getFilesDir(), "sdk.zip"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
                ZipFile zipFile = new ZipFile(new File(context.getFilesDir(), "sdk.zip").getAbsolutePath());
                zipFile.extractAll(sdk.getAbsolutePath());
                new File(context.getFilesDir(), "sdk.zip").delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            for (File file : libs.listFiles()) {
                if (file.getName().endsWith(".aar")) {
                    ZipFile zipFile = new ZipFile(new File(libs, file.getName()).getAbsolutePath());
                    zipFile.extractAll(new File(aarLibs + File.separator + file.getName().replace(".aar", "")).getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (androidxLibs.listFiles().length == 0) {
            try {
                InputStream input = context.getAssets().open("libs/androidx.zip");
                OutputStream output = new FileOutputStream(new File(context.getFilesDir(), "androidx.zip"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
                ZipFile zipFile = new ZipFile(new File(context.getFilesDir(), "androidx.zip").getAbsolutePath());
                zipFile.extractAll(androidxLibs.getAbsolutePath());
                new File(context.getFilesDir(), "androidx.zip").delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!new File(keys, "testkey.x509.pem").exists()) {
            try {
                InputStream input = context.getAssets().open("key/testkey.x509.pem");
                OutputStream output = new FileOutputStream(new File(keys, "testkey.x509.pem"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!new File(keys, "testkey.pk8").exists()) {
            try {
                InputStream input = context.getAssets().open("key/testkey.pk8");
                OutputStream output = new FileOutputStream(new File(keys, "testkey.pk8"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (jars.listFiles().length == 0) {
            try {
                String[] list = context.getAssets().list("jars");
                for (String s : list) {
                    InputStream input = context.getAssets().open("jars/" + s);
                    OutputStream output = new FileOutputStream(new File(jars, s));
                    IOUtils.copy(input, output);
                    input.close();
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            GsonBuilder builder = new GsonBuilder();
            Gson gson = builder.create();
            Android content = gson.fromJson(FileUtils.readFile(project + File.separator + "app/build.json"), Android.class);

            packageName = content.defaultConfig.applicationId;
            versionName = content.defaultConfig.versionName;
            versionCode = content.defaultConfig.versionCode;
            minSdkVersion = content.defaultConfig.minSdkVersion;
            targetSdkVersion = content.defaultConfig.targetSdkVersion;
            isAndroidX = content.dependencies.androidx;
            isDebug = content.buildTypes.debug;
            isMinify = content.buildTypes.minify;
            isJava8 = content.buildTypes.java8;
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            for (File f : libs.listFiles()) {
                if (!allLibs.contains(f.getName())) {
                    allLibs.add(f);
                }
            }
            if (isAndroidX) {
                for (File f : androidxLibs.listFiles()) {
                    if (!allLibs.contains(f.getName())) {
                        allLibs.add(f);
                    }
                }
            }

            for (File f : aarLibs.listFiles()) {
                if (!allLibs.contains(f.getName())) {
                    allLibs.add(f);
                }
            }

            StringBuilder s = new StringBuilder();
            for (File f : allLibs) {
                s.append(f.getAbsolutePath());
                s.append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            publishProgress("Merging manifest...");
            mergeManifest();

            if (new File(project + File.separator + "app/google-services.json").exists()) {
                String content = FileUtils.readFile(project + File.separator + "app/google-services.json");
                GsonBuilder builder = new GsonBuilder();
                Gson gson = builder.create();

                Firebase firebase = gson.fromJson(content, Firebase.class);

                defaultWebClientId = firebase.client.get(0).oauthClient.get(0).clientId; // client_id

                firebaseDatabaseUrl = firebase.projectInfo.firebaseUrl; // firebase_url
                gcmDefaultSenderId = firebase.projectInfo.projectNumber; // project_number

                googleApiKey = firebase.client.get(0).apiKey.get(0).currentKey; // current_key
                googleAppId = firebase.client.get(0).clientInfo.mobilesdkAppId; // mobilesdk_app_id
                googleCrashReportingApiKey = firebase.client.get(0).apiKey.get(0).currentKey; // current_key

                googleStorageBucket = firebase.projectInfo.storageBucket; // storage_bucket
                projectId = firebase.projectInfo.projectId; // project_id

                String firebaseConfig = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<resources>\n" +
                        "    <string name=\"default_web_client_id\" translatable=\"false\">DEFAULT_WEB_CLIENT_ID</string>\n".replace("DEFAULT_WEB_CLIENT_ID", defaultWebClientId) +
                        "    <string name=\"firebase_database_url\" translatable=\"false\">FIREBASE_DATABASE_URL</string>\n".replace("FIREBASE_DATABASE_URL", firebaseDatabaseUrl) +
                        "    <string name=\"gcm_defaultSenderId\" translatable=\"false\">GCM_DEFAULT_SENDER_ID</string>\n".replace("GCM_DEFAULT_SENDER_ID", gcmDefaultSenderId) +
                        "    <string name=\"google_api_key\" translatable=\"false\">GOOGLE_API_KEY</string>\n".replace("GOOGLE_API_KEY", googleApiKey) +
                        "    <string name=\"google_app_id\" translatable=\"false\">GOOGLE_APP_ID</string>\n".replace("GOOGLE_APP_ID", googleAppId) +
                        "    <string name=\"google_crash_reporting_api_key\" translatable=\"false\">GOOGLE_CRASH_REPORTING_API_KEY</string>\n".replace("GOOGLE_CRASH_REPORTING_API_KEY", googleCrashReportingApiKey) +
                        "    <string name=\"google_storage_bucket\" translatable=\"false\">GOOGLE_STORAGE_BUCKET</string>\n".replace("GOOGLE_STORAGE_BUCKET", googleStorageBucket) +
                        "    <string name=\"project_id\" translatable=\"false\">PROJECT_ID</string>\n".replace("PROJECT_ID", projectId) +
                        "</resources>";
                FileUtils.writeFile(genFirebase.getAbsolutePath() + File.separator + "values" + File.separator + "values.xml", firebaseConfig);
            }

            publishProgress("Aapt runing...");
            runAapt();

            String buildConfig = "package $PACKAGE$;\n".replace("$PACKAGE$", packageName) +
                    "\n" +
                    "public final class BuildConfig {\n" +
                    "  public static final boolean DEBUG = Boolean.parseBoolean(\"$DEBUG$\");\n".replace("$DEBUG$", String.valueOf(isDebug)) +
                    "  public static final String APPLICATION_ID = \"$PACKAGE$\";\n".replace("$PACKAGE$", packageName) +
                    "  public static final String BUILD_TYPE = \"$DEBUG$\";\n".replace("$DEBUG$", isDebug ? "debug" : "release") +
                    "  public static final int VERSION_CODE = $VERSION_CODE$;\n".replace("$VERSION_CODE$", versionCode) +
                    "  public static final String VERSION_NAME = \"$VERSION_NAME$\";\n".replace("$VERSION_NAME$", versionName) +
                    "}";
            FileUtils.writeFile(gen.getAbsolutePath() + "/" + packageName.replace(".", "/") + "/BuildConfig.java", buildConfig);

            publishProgress("Compiling java...");
            runEcj();

            if (!isMinify) {
                publishProgress("Dexing...");
                runDex();
            }

            publishProgress("Merging classes...");
            if (isMinify) {
                mergeDexR8();
            } else {
                mergeDex();
            }

            if (!isMinify) {
                extractFileFromJars();
            }

            publishProgress("Building Apk...");
            addFilesInApk();

            publishProgress("Aligning Apk...");
            runZipalign();

            publishProgress("Signing Apk...");
            signApk();
        } catch (Exception e) {
            e.printStackTrace();
            this.cancel(true);
            return e.getMessage();
        }
        return "";
    }

    private void signApk() throws Exception {
        StringBuilder cmd = new StringBuilder();
        cmd.append("dalvikvm -Xcompiler-option --compiler-filter=speed -Xmx256m -cp " + jars.getAbsolutePath() + "/apksigner.jar" + " com.android.apksigner.ApkSignerTool");
        cmd.append(" sign");
        cmd.append(" --key " + keys.getAbsolutePath() + "/testkey.pk8");
        cmd.append(" --cert " + keys.getAbsolutePath() + "/testkey.x509.pem");
        cmd.append(" --out " + release.getAbsolutePath() + "/app-signed.apk");
        cmd.append(" --in " + buildBin.getAbsolutePath() + "/app-aligned.apk");
        Process dexProcess = Runtime.getRuntime().exec(cmd.toString());
        String error = FileUtils.readInputStreem(dexProcess.getErrorStream());
        if (!error.isEmpty()) {
            if (error.contains("ERROR:")) {
                throw new Exception(error);
            }
        }
    }

    private void addFilesInApk() {
        List<File> allLib = new ArrayList<>();
        for (File f : allLibs) {
            File lib = new File(f, "lib");
            if (lib.exists()) {
                allLib.add(lib);
            }
        }
        try {
            ZipFile zip = new ZipFile(buildBin.getAbsolutePath() + "/app.apk");
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            for (File f : dexes.listFiles()) {
                if (f.isDirectory()) {
                    zip.addFolder(f, zipParameters);
                } else {
                    zip.addFile(f, zipParameters);
                }
            }
            if (jniLibs.exists()) {
                if (jniLibs.listFiles().length != 0) {
                    zip.addFolder(jniLibs, zipParameters);
                }
            }
            for (File f : allLib) {
                zip.addFolder(f, zipParameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void extractFileFromJars() {
        List<String> allJar = new ArrayList<>();
        for (File f : allLibs) {
            File jar = new File(f, "classes.jar");
            if (jar.exists()) {
                allJar.add(jar.getAbsolutePath());
            }
        }
        try {
            for (String s : allJar) {
                ZipFile zip = new ZipFile(s);
                for (FileHeader each : (List<FileHeader>) zip.getFileHeaders()) {
                    String fileName = each.getFileName();
                    if (fileName.endsWith(".class")) {
                        continue;
                    }
                    if (each.isDirectory()) {
                        continue;
                    }
                    zip.extractFile(fileName, dexes.getAbsolutePath());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeDexR8() throws Exception {
        List<String> allJar = new ArrayList<>();
        for (File f : allLibs) {
            File jar = new File(f, "classes.jar");
            if (jar.exists()) {
                allJar.add(jar.getAbsolutePath());
            }
        }
        List<String> allPro = new ArrayList<>();
        for (File f : allLibs) {
            File pro = new File(f, "proguard.txt");
            if (pro.exists()) {
                allPro.add(pro.getAbsolutePath());
            }
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append("dalvikvm -Xcompiler-option --compiler-filter=speed -Xmx512m -cp " + jars.getAbsolutePath() + "/d8.jar" + " com.android.tools.r8.R8");
        cmd.append(" --release");
        cmd.append(" --lib " + sdk.getAbsolutePath() + "/android.jar");
        if (isJava8) {
            cmd.append(" --classpath " + sdk.getAbsolutePath() + "/rt.jar");
        } else {
            cmd.append(" --no-desugaring");
        }
        cmd.append(" --min-api " + minSdkVersion);
        //cmd.append(" --no-tree-shaking");
        //cmd.append(" --no-minification");
        cmd.append(" --pg-map-output " + buildBin.getAbsolutePath() + "/mapping.txt");
        cmd.append(" --output " + dexes.getAbsolutePath());
        cmd.append(" --pg-conf " + buildBin.getAbsolutePath() + "/aapt-rules.txt");
        cmd.append(" --pg-conf " + context.getFilesDir() + "/proguard.txt");
        cmd.append(" --pg-conf " + new File(project, "app/proguard-rules.pro").getAbsolutePath());
        for (String s : allPro) {
            cmd.append(" --pg-conf " + s);
        }
        getAllFilesOfDir(".class", classes.getAbsolutePath(), cmd);
        for (String s : allJar) {
            cmd.append(" ");
            cmd.append(s);
        }
        Process dexProcess = Runtime.getRuntime().exec(cmd.toString());
        String error = FileUtils.readInputStreem(dexProcess.getErrorStream());
        if (!error.isEmpty()) {
            if (error.contains("Error")) {
                throw new Exception(error);
            }
        }
    }

    private void mergeDex() throws Exception {
        List<String> allJar = new ArrayList<>();
        for (File f : allLibs) {
            File jar = new File(f, "classes.jar");
            if (jar.exists()) {
                allJar.add(jar.getAbsolutePath());
            }
        }

        StringBuilder cmd = new StringBuilder();
        cmd.append("dalvikvm -Xcompiler-option --compiler-filter=speed -Xmx256m -cp " + jars.getAbsolutePath() + "/d8.jar" + " com.android.tools.r8.D8");
        cmd.append(" --release");
        cmd.append(" --min-api " + minSdkVersion);
        cmd.append(" --output " + dexes.getAbsolutePath());
        cmd.append(" --intermediate");
        getAllFilesOfDir(".dex", classes.getAbsolutePath(), cmd);
        for (String s : allJar) {
            cmd.append(" ");
            cmd.append(s);
        }
        Process dexProcess = Runtime.getRuntime().exec(cmd.toString());
        String error = FileUtils.readInputStreem(dexProcess.getErrorStream());
        if (!error.isEmpty()) {
            if (error.contains("Error")) {
                throw new Exception(error);
            }
        }
    }

    private void runDex() throws Exception {
        StringBuilder cmd = new StringBuilder();
        cmd.append("dalvikvm -Xcompiler-option --compiler-filter=speed -Xmx256m -cp " + jars.getAbsolutePath() + "/d8.jar" + " com.android.tools.r8.D8");
        cmd.append(" --release");
        cmd.append(" --lib " + sdk.getAbsolutePath() + "/android.jar");
        cmd.append(" --min-api " + minSdkVersion);
        if (isJava8) {
            cmd.append(" --classpath " + sdk.getAbsolutePath() + "/rt.jar");
        } else {
            cmd.append(" --no-desugaring");
        }
        cmd.append(" --output " + classes.getAbsolutePath());
        cmd.append(" --intermediate");
        cmd.append(" --file-per-class");
        getAllFilesOfDir(".class", classes.getAbsolutePath(), cmd);
        Process dexProcess = Runtime.getRuntime().exec(cmd.toString());
        String error = FileUtils.readInputStreem(dexProcess.getErrorStream());
        if (!error.isEmpty()) {
            if (error.contains("Error")) {
                throw new Exception(error);
            }
        }
    }

    private void getAllFilesOfDir(String s, String path, StringBuilder sb) {
        File file = new File(path);
        for (File x : file.listFiles()) {
            if (x.isDirectory()) {
                getAllFilesOfDir(s, x.getAbsolutePath(), sb);
            } else if (x.isFile()) {
                if (x.getName().endsWith(s)) {
                    sb.append(" ");
                    sb.append(x.getAbsolutePath());
                }
            }
        }
    }

    private void runEcj() throws Exception {
        StringBuilder allJars = new StringBuilder();
        for (File f : allLibs) {
            File jar = new File(f, "classes.jar");
            if (jar.exists()) {
                if (allJars.length() == 0) {
                    allJars.append(jar.getAbsolutePath());
                } else {
                    allJars.append(":");
                    allJars.append(jar.getAbsolutePath());
                }
            }
        }
        StringBuilder allJava = new StringBuilder();
        for (File f : allLibs) {
            File java = new File(f, "java");
            if (java.exists()) {
                if (allJava.length() == 0) {
                    allJava.append(java.getAbsolutePath());
                } else {
                    allJava.append(":");
                    allJava.append(java.getAbsolutePath());
                }
            }
        }
        List<String> allsource = new ArrayList<>();
        for (File f : allLibs) {
            File s = new File(f, "java");
            if (s.exists()) {
                allsource.add(s.getAbsolutePath());
            }
        }
        StringWriter errResult = new StringWriter();
        PrintWriter errWriter = new PrintWriter(errResult);
        List<String> cmd = new ArrayList<>();
        cmd.add("-proc:none");
        cmd.add("-nowarn");
        if (isJava8) {
            cmd.add("-8");
        } else {
            cmd.add("-7");
        }
        cmd.add("-deprecation");
        cmd.add("-d");
        cmd.add(classes.getAbsolutePath());
        cmd.add("-bootclasspath");
        cmd.add(sdk.getAbsolutePath() + "/android.jar");
        cmd.add("-cp");
        cmd.add(allJars.toString());
        if (isJava8) {
            cmd.add("-cp");
            cmd.add(sdk.getAbsolutePath() + "/rt.jar");
        }
        cmd.add("-sourcepath");
        cmd.add(gen.getAbsolutePath() + ":" + new File(project, "app/src/main/java").getAbsolutePath() + ":" + allJava.toString());
        cmd.add(gen.getAbsolutePath());
        cmd.add(new File(project, "app/src/main/java").getAbsolutePath());
        for (String s : allsource) {
            cmd.add(s);
        }
        Compiler cm = new Compiler();
        cm.main(context, cmd.toArray(new String[0]), errWriter);
        String error = errResult.toString();
        errWriter.close();
        if (!cm.isErrors()) {
            throw new Exception(error);
        }
    }

    private void runAapt() throws Exception {
        List<String> allRes = new ArrayList<>();
        for (File f : allLibs) {
            File res = new File(f, "res");
            if (res.exists()) {
                allRes.add(res.getAbsolutePath());
            }
        }
        List<String> allAssets = new ArrayList<>();
        for (File f : allLibs) {
            File assets = new File(f, "assets");
            if (assets.exists()) {
                allAssets.add(assets.getAbsolutePath());
            }
        }
        StringBuilder allPackages = new StringBuilder();
        for (File f : allLibs) {
            File pack = new File(f, "package.txt");
            if (pack.exists()) {
                if (allPackages.length() == 0) {
                    allPackages.append(FileUtils.readFile(pack.getAbsolutePath()));
                } else {
                    allPackages.append(":");
                    allPackages.append(FileUtils.readFile(pack.getAbsolutePath()));
                }
            }
        }
        List<String> ch = new ArrayList<>();
        ch.add("chmod");
        ch.add("744");
        ch.add(bin.getAbsolutePath() + "/aapt");
        Runtime.getRuntime().exec(ch.toArray(new String[0]));

        List<String> cmd = new ArrayList<>();
        cmd.add(bin.getAbsolutePath() + "/aapt");
        cmd.add("package");
        cmd.add("-f");
        cmd.add("--auto-add-overlay");
        cmd.add("-M");
        cmd.add(buildBin.getAbsolutePath() + "/AndroidManifest.xml");
        cmd.add("-F");
        cmd.add(buildBin.getAbsolutePath() + "/app.apk");
        cmd.add("-I");
        cmd.add(sdk.getAbsolutePath() + "/android.jar");
        if (new File(project, "app/src/main/assets").exists()) {
            cmd.add("-A");
            cmd.add(new File(project, "app/src/main/assets").getAbsolutePath());
        }
        if (new File(project, "app/src/main/res").exists()) {
            cmd.add("-S");
            cmd.add(new File(project, "app/src/main/res").getAbsolutePath());
        }
        if (new File(project, "app/build/res").exists()) {
            cmd.add("-S");
            cmd.add(new File(project, "app/build/res").getAbsolutePath());
        }

        cmd.add("-m");
        cmd.add("-J");
        cmd.add(gen.getAbsolutePath());
        for (String s : allRes) {
            cmd.add("-S");
            cmd.add(s);
        }
        for (String s : allAssets) {
            cmd.add("-A");
            cmd.add(s);
        }
        cmd.add("--extra-packages");
        cmd.add(allPackages.toString());
        cmd.add("--min-sdk-version");
        cmd.add(minSdkVersion);
        cmd.add("--target-sdk-version");
        cmd.add(targetSdkVersion);
        cmd.add("--version-code");
        cmd.add(versionCode);
        cmd.add("--version-name");
        cmd.add(versionName);
        cmd.add("--no-version-vectors");
        cmd.add("-G");
        cmd.add(buildBin.getAbsolutePath() + "/aapt-rules.txt");
        Process aaptProcess = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
        String error = FileUtils.readInputStreem(aaptProcess.getErrorStream());
        if (!error.isEmpty()) {
            throw new Exception(error);
        }
    }

    private void runZipalign() throws Exception {
        List<String> ch = new ArrayList<>();
        ch.add("chmod");
        ch.add("744");
        ch.add(bin.getAbsolutePath() + "/zipalign");
        Runtime.getRuntime().exec(ch.toArray(new String[0]));

        List<String> cmd = new ArrayList<>();
        cmd.add(bin.getAbsolutePath() + "/zipalign");
        cmd.add("-p");
        cmd.add("-f");
        cmd.add("-v");
        cmd.add("4");
        cmd.add(buildBin.getAbsolutePath() + "/app.apk");
        cmd.add(buildBin.getAbsolutePath() + "/app-aligned.apk");
        Process zipalignProcess = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
        String error = FileUtils.readInputStreem(zipalignProcess.getErrorStream());
        if (!error.isEmpty()) {
            throw new Exception(error);
        }
    }

    private void mergeManifest() throws Exception {
        String main = project + "/app/src/main/AndroidManifest.xml";
        String output = buildBin + "/AndroidManifest.xml";
        int i = 0;
        for (File f : allLibs) {
            File f2 = new File(f, "AndroidManifest.xml");
            if (f2.exists()) {
                i++;
                FileUtils.writeFile(temp.getAbsolutePath() + "/" + i + ".xml", FileUtils.readFile(f2.getAbsolutePath()).replace("${applicationId}", packageName));
            }
        }
        List<String> manifests = new ArrayList<>();
        for (File f : temp.listFiles()) {
            manifests.add(f.getAbsolutePath());
        }

        String error = Merger.merge(context, main, manifests.toArray(new String[0]), output);
        if (error != null) {
            throw new Exception(error);
        }
    }

    @Override
    protected void onProgressUpdate(String[] values) {
        callback.onProgress(values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String result) {
        if (!result.isEmpty()) {
            callback.onFailure(result);
        }
        super.onCancelled(result);
    }

    @Override
    protected void onPostExecute(String result) {
        callback.onSuccess(null);
        super.onPostExecute(result);
    }
}