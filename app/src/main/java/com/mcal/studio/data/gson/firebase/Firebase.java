package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

// Main class

public class Firebase {

    @SerializedName("project_info")
    @Expose
    public ProjectInfo projectInfo;
    @SerializedName("client")
    @Expose
    public List<Client> client = null;
    @SerializedName("configuration_version")
    @Expose
    public String configurationVersion;

    /**
     * @param projectInfo
     * @param configurationVersion
     * @param client
     */
    public Firebase(ProjectInfo projectInfo, List<Client> client, String configurationVersion) {
        super();
        this.projectInfo = projectInfo;
        this.client = client;
        this.configurationVersion = configurationVersion;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public List<Client> getClient() {
        return client;
    }

    public String getConfigurationVersion() {
        return configurationVersion;
    }
}