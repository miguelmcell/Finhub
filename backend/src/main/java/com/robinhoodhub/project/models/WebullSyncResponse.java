package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WebullSyncResponse {
    private String access_token;
    private String account_id;
    private String refresh_token;
    private String expirationTime;

    public WebullSyncResponse() {
        super();
    }
    public WebullSyncResponse(String access_token, String account_id, String refresh_token, String expirationTime){
        this.access_token = access_token;
        this.account_id = account_id;
        this.refresh_token = refresh_token;
        this.expirationTime = expirationTime;
    }
}
