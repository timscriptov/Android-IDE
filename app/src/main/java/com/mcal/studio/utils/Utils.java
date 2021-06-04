package com.mcal.studio.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.TypedValue;
import android.widget.Toast;

import com.mcal.studio.App;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

public class Utils {

    public static String getRandom() {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";//#
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    public static void copyResources(int res, String path) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = App.getContext().getResources().openRawResource(res);
            out = new FileOutputStream(new File(path));
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer, 0, buffer.length)) != -1) {
                out.write(buffer, 0, len);
            }
            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readFile(String str) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(str)));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                sb.append(line);
                sb.append('\n');
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }

    public static String readAssest(String str) {
        String tContents = null;
        try {
            InputStream input = App.getContext().getAssets().open(str);
            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();
            tContents = new String(buffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return tContents;
    }

    public static void copyFileFromAssets(String name, String destFile) {
        try {
            File d = new File(destFile);
            d.getParentFile().mkdirs();
            AssetManager am = App.getContext().getAssets();
            InputStream inputStream = am.open(name);
            FileOutputStream outputStream = new FileOutputStream(d);
            byte buffer[] = new byte[1024];
            int length = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void toast(Context _context, String _s) {
        Toast.makeText(_context, _s, Toast.LENGTH_SHORT).show();
    }

    public static int getRandom(int _min, int _max) {
        Random random = new Random();
        return random.nextInt(_max - _min + 1) + _min;
    }

    public static float getDip(Context _context, int _input) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, _input, _context.getResources().getDisplayMetrics());
    }

    public static int getDisplayWidthPixels(Context _context) {
        return _context.getResources().getDisplayMetrics().widthPixels;
    }

}
