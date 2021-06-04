package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OtherPlatformOauthClient {

    @SerializedName("client_id")
    @Expose
    public String clientId;
    @SerializedName("client_type")
    @Expose
    public Integer clientType;

    /**
     * @param clientId
     * @param clientType
     */
    public OtherPlatformOauthClient(String clientId, Integer clientType) {
        super();
        this.clientId = clientId;
        this.clientType = clientType;
    }

}