package com.mcal.studio.data;

import android.os.Environment;

import java.io.File;

public class Constants {

    public static final File PROJECT_DIR = new File(Environment.getExternalStorageDirectory(), "AppProjects");

    public Constants() {
        if (!PROJECT_DIR.exists()) {
            PROJECT_DIR.mkdirs();
        }
    }
}