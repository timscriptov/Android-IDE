package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ProjectInfo {

    @SerializedName("project_number")
    @Expose
    public String projectNumber;
    @SerializedName("firebase_url")
    @Expose
    public String firebaseUrl;
    @SerializedName("project_id")
    @Expose
    public String projectId;
    @SerializedName("storage_bucket")
    @Expose
    public String storageBucket;

    /**
     * @param storageBucket
     * @param firebaseUrl
     * @param projectNumber
     * @param projectId
     */
    public ProjectInfo(String projectNumber, String firebaseUrl, String projectId, String storageBucket) {
        super();
        this.projectNumber = projectNumber;
        this.firebaseUrl = firebaseUrl;
        this.projectId = projectId;
        this.storageBucket = storageBucket;
    }
}