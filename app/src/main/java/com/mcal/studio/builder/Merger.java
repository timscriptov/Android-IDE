package com.mcal.studio.builder;

import android.content.Context;

import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class Merger {

    private static String error;

    public static String merge(Context context, String main, String[] libs, String output) {
        DexClassLoader dexClassLoader = new DexClassLoader(new File(context.getFilesDir(), "jars/merger.jar").getAbsolutePath(), context.getDir("dex", 0).getPath(), null, ClassLoader.getSystemClassLoader());
        try {
            Class<Object> cp = (Class<Object>) dexClassLoader.loadClass("com.mcal.manifestmerger.Merger");
            Object ob = cp.newInstance();
            Method m = cp.getDeclaredMethod("merge", new Class[]{String.class, String[].class, String.class});
            error = (String) m.invoke(ob, new Object[]{main, libs, output});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return error;
    }

    public static String merge(Context c, String[] args) {
        DexClassLoader dexClassLoader = new DexClassLoader(new File(c.getFilesDir(), "jars/merger.jar").getAbsolutePath(), c.getDir("dex", 0).getPath(), null, ClassLoader.getSystemClassLoader());
        try {
            Class<Object> cp = (Class<Object>) dexClassLoader.loadClass("com.android.manifmerger.Merger");
            Object ob = cp.newInstance();
            Method m = cp.getDeclaredMethod("main", new Class[]{String[].class});
            error = (String) m.invoke(ob, new Object[]{args});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return error;
    }
}

