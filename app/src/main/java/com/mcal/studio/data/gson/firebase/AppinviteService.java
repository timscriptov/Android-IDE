package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AppinviteService {

    @SerializedName("other_platform_oauth_client")
    @Expose
    public List<OtherPlatformOauthClient> otherPlatformOauthClient = null;

    /**
     * @param otherPlatformOauthClient
     */
    public AppinviteService(List<OtherPlatformOauthClient> otherPlatformOauthClient) {
        super();
        this.otherPlatformOauthClient = otherPlatformOauthClient;
    }
}