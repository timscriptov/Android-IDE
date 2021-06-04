package com.mcal.studio.helper;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

import com.mcal.studio.R;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Helper class to validate project creation
 */
public class DataValidator {

    /**
     * Method to validate project creation
     *
     * @param appName        of project
     * @param appPackageName of project
     * @return true if valid
     */
    public static boolean validateCreate(Context context, @Nullable AppCompatEditText appName, AppCompatEditText appPackageName) {
        if (appName != null) {
            if (appName.getText().toString().isEmpty()) {
                appName.setError(context.getString(R.string.name_error));
                return false;
            }
        }

        if (appPackageName.getText().toString().isEmpty()) {
            appPackageName.setError("Please enter package name");
            return false;
        }

        return true;
    }

    public static boolean validateClone(Context context, AppCompatEditText name, AppCompatEditText remote) {
        if (name.getText().toString().isEmpty()) {
            name.setError(context.getString(R.string.name_error));
            return false;
        }

        if (remote.getText().toString().isEmpty()) {
            remote.setError(context.getString(R.string.remote_error));
            return false;
        }

        return true;
    }

    /**
     * Removes broken projects from list
     *
     * @param objectsList to remove projects from
     */
    public static void removeBroken(ArrayList objectsList) {
        for (Iterator iterator = objectsList.iterator(); iterator.hasNext(); ) {
            String string = (String) iterator.next();
            if (!ProjectManager.isValid(string)) {
                iterator.remove();
            }
        }
    }
}
