package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ClientInfo {

    @SerializedName("mobilesdk_app_id")
    @Expose
    public String mobilesdkAppId;
    @SerializedName("android_client_info")
    @Expose
    public AndroidClientInfo androidClientInfo;

    /**
     * @param androidClientInfo
     * @param mobilesdkAppId
     */
    public ClientInfo(String mobilesdkAppId, AndroidClientInfo androidClientInfo) {
        super();
        this.mobilesdkAppId = mobilesdkAppId;
        this.androidClientInfo = androidClientInfo;
    }
}