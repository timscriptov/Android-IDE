package com.mcal.studio.data.gson.build;

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
    @SerializedName("dependencies")
    public DependenciesJava dependencies;

    public Android(String compileSdkVersion, String buildToolsVersion, DefaultConfig defaultConfig, BuildTypes buildTypes, DependenciesJava dependencies) {
        this.compileSdkVersion = compileSdkVersion;
        this.buildToolsVersion = buildToolsVersion;
        this.defaultConfig = defaultConfig;
        this.buildTypes = buildTypes;
        this.dependencies = dependencies;
    }
}