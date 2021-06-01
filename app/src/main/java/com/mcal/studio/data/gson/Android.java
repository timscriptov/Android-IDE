package com.mcal.studio.data.gson;

import com.google.gson.annotations.SerializedName;

public class Android {
    @SerializedName("compileSdkVersion")
    public String compileSdkVersion;
    @SerializedName("buildToolsVersion")
    public String buildToolsVersion;
    @SerializedName("defaultConfig")
    public DefaultConfig defaultConfig;
    @SerializedName("buildTypes")
    public BuildTypes buildTypes;

    public Android(String compileSdkVersion, String buildToolsVersion, DefaultConfig defaultConfig, BuildTypes buildTypes) {
        this.compileSdkVersion = compileSdkVersion;
        this.buildToolsVersion = buildToolsVersion;
        this.defaultConfig = defaultConfig;
        this.buildTypes = buildTypes;
    }
}