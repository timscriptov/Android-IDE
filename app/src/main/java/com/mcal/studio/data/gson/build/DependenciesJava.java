package com.mcal.studio.data.gson.build;

import com.google.gson.annotations.SerializedName;

public class DependenciesJava {
    @SerializedName("androidx")
    public boolean androidx;
    @SerializedName("firebase")
    public boolean firebase;

    public DependenciesJava(boolean androidx, boolean firebase) {
        this.androidx = androidx;
        this.firebase = firebase;
    }
}
