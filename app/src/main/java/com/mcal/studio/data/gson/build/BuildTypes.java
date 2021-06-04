package com.mcal.studio.data.gson.build;

import com.google.gson.annotations.SerializedName;

public class BuildTypes {
    @SerializedName("minify")
    public boolean minify;
    @SerializedName("debug")
    public boolean debug;
    @SerializedName("java8")
    public boolean java8;

    public BuildTypes(boolean minify, boolean debug, boolean java8) {
        this.minify = minify;
        this.debug = debug;
        this.java8 = java8;
    }
}
