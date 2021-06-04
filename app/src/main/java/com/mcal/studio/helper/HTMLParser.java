package com.mcal.studio.helper;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

public class HTMLParser {

    private static final String TAG = HTMLParser.class.getSimpleName();

    private static Document getSoup(String name) {
        try {
            return Jsoup.parse(ProjectManager.getIndexFile(name), "UTF-8");
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }

    public static String[] getProperties(String projName, String projectDir) {
        Document soup = getSoup(projName);
        String[] properties = new String[4];

        if (soup != null) {
            // title
            properties[0] = projName;
            properties[1] = projectDir;
        }

        return properties;
    }
}
