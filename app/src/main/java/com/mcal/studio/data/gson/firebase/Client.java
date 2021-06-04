package com.mcal.studio.data.gson.firebase;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Client {

    @SerializedName("client_info")
    @Expose
    public ClientInfo clientInfo;
    @SerializedName("oauth_client")
    @Expose
    public List<OauthClient> oauthClient = null;
    @SerializedName("api_key")
    @Expose
    public List<ApiKey> apiKey = null;
    @SerializedName("services")
    @Expose
    public Services services;

    /**
     * @param apiKey
     * @param clientInfo
     * @param services
     * @param oauthClient
     */
    public Client(ClientInfo clientInfo, List<OauthClient> oauthClient, List<ApiKey> apiKey, Services services) {
        super();
        this.clientInfo = clientInfo;
        this.oauthClient = oauthClient;
        this.apiKey = apiKey;
        this.services = services;
    }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public List<OauthClient> getOauthClient() {
        return oauthClient;
    }

    public List<ApiKey> getApiKey() {
        return apiKey;
    }

    public Services getServices() {
        return services;
    }
}