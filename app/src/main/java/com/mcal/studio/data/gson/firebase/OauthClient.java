package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class OauthClient {

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
    public OauthClient(String clientId, Integer clientType) {
        super();
        this.clientId = clientId;
        this.clientType = clientType;
    }

    public String getClientId() {
        return clientId;
    }

    public Integer getClientType() {
        return clientType;
    }

}