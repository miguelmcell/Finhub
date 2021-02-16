package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RobinhoodSyncResponse {
    private String access_token;
    private String refresh_token;
    private float expires_in;
    public RobinhoodSyncResponse()
    {
        super();
    }
    public RobinhoodSyncResponse(String access_token, String refresh_token, float expires_in) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.expires_in = expires_in;
    }
}
