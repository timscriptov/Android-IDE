package com.mcal.studio.utils;

import android.os.Build;

import java.io.File;

public class AbiInfo {
    public static String getBinaryName(String name) {
        return Build.SUPPORTED_ABIS[0] + File.separator + name;
    }
}
