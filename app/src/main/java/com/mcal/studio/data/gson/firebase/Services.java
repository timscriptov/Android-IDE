package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Services {

    @SerializedName("appinvite_service")
    @Expose
    public AppinviteService appinviteService;

    /**
     * @param appinviteService
     */
    public Services(AppinviteService appinviteService) {
        super();
        this.appinviteService = appinviteService;
    }
}