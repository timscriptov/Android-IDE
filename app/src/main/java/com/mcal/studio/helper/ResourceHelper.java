package com.mcal.studio.helper;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.TypedValue;

import com.mcal.studio.R;

import java.io.File;

/**
 * Helper class used for decor related functions
 */
public class ResourceHelper {

    /**
     * Method to prevent OOM errors when calling setImageBitmap()
     *
     * @param selectedImage uri of the image
     * @return resized bitmap
     */
    public static Bitmap decodeUri(Context context, Uri selectedImage) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o);

            // The new size we want to scale to
            final int REQUIRED_SIZE = 140;

            // Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(selectedImage), null, o2);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getIcon(File file) {
        String fileName = file.getName();
        if (file.isDirectory()) return R.drawable.ic_folder;
        if (ProjectManager.isImageFile(file)) return R.drawable.ic_image;
        if (ProjectManager.isBinaryFile(file)) return R.drawable.ic_binary;
        if (fileName.endsWith(".aar")) return R.drawable.ic_aar;
        if (fileName.endsWith(".c")) return R.drawable.ic_c;
        if (fileName.endsWith(".cpp")) return R.drawable.ic_cpp;
        if (fileName.endsWith(".cs")) return R.drawable.ic_csharp;
        if (fileName.endsWith(".css")) return R.drawable.ic_css;
        if (fileName.endsWith(".gradle")) return R.drawable.ic_gradle;
        if (fileName.endsWith(".h")) return R.drawable.ic_h;
        if (fileName.endsWith(".hpp")) return R.drawable.ic_hpp;
        if (fileName.endsWith(".htm")) return R.drawable.ic_html;
        if (fileName.endsWith(".html")) return R.drawable.ic_html;
        if (fileName.endsWith(".jar")) return R.drawable.ic_jar;
        if (fileName.endsWith(".java")) return R.drawable.ic_java;
        if (fileName.endsWith(".js")) return R.drawable.ic_js;
        if (fileName.endsWith(".json")) return R.drawable.ic_json;
        if (fileName.endsWith(".kt")) return R.drawable.ic_kotlin;
        if (fileName.endsWith(".lua")) return R.drawable.ic_lua;
        if (fileName.endsWith("AndroidManifest.xml")) return R.drawable.ic_mf;
        if (fileName.endsWith(".php")) return R.drawable.ic_php;
        if (fileName.endsWith(".rar")) return R.drawable.ic_rar;
        if (fileName.endsWith(".txt")) return R.drawable.ic_txt;
        if (fileName.endsWith(".xml")) return R.drawable.ic_xml;
        if (fileName.endsWith(".zip")) return R.drawable.ic_zip;
        if (fileName.endsWith(".woff") || fileName.endsWith(".ttf") || fileName.endsWith(".otf") || fileName.endsWith(".woff2") || fileName.endsWith(".fnt"))
            return R.drawable.ic_font;
        return R.drawable.ic_unknown;
    }

    /**
     * Utility method to convert from dp to pixels
     *
     * @param context context to get resources
     * @param dp      to convert
     * @return value in px
     */
    public static int dpToPx(Context context, int dp) {
        Resources r = context.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

}
