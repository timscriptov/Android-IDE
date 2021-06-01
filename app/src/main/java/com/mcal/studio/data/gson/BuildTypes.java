package com.mcal.studio.data.gson;

import com.google.gson.annotations.SerializedName;

public class BuildTypes {
    @SerializedName("androidx")
    public boolean androidx;
    @SerializedName("minify")
    public boolean minify;
    @SerializedName("debug")
    public boolean debug;
    @SerializedName("java8")
    public boolean java8;

    public BuildTypes(boolean androidx, boolean minify, boolean debug, boolean java8) {
        this.androidx = androidx;
        this.minify = minify;
        this.debug = debug;
        this.java8 = java8;
    }
}
