package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ApiKey {
    @SerializedName("current_key")
    @Expose
    public String currentKey;

    /**
     * @param currentKey
     */
    public ApiKey(String currentKey) {
        super();
        this.currentKey = currentKey;
    }
}