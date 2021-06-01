package com.mcal.studio.data.gson;

import com.google.gson.annotations.SerializedName;

public class DefaultConfig {
    @SerializedName("applicationId")
    public String applicationId;
    @SerializedName("minSdkVersion")
    public String minSdkVersion;
    @SerializedName("targetSdkVersion")
    public String targetSdkVersion;
    @SerializedName("versionCode")
    public String versionCode;
    @SerializedName("versionName")
    public String versionName;

    public DefaultConfig(String applicationId, String minSdkVersion, String targetSdkVersion, String versionCode, String versionName) {
        this.applicationId = applicationId;
        this.minSdkVersion = minSdkVersion;
        this.targetSdkVersion = targetSdkVersion;
        this.versionCode = versionCode;
        this.versionName = versionName;
    }
}
