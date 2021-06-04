package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AndroidClientInfo {

    @SerializedName("package_name")
    @Expose
    public String packageName;

    /**
     * @param packageName
     */
    public AndroidClientInfo(String packageName) {
        super();
        this.packageName = packageName;
    }
}