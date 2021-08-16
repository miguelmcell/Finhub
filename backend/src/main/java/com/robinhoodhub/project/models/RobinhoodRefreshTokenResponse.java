package com.robinhoodhub.project.models;

import lombok.Data;

@Data
public class RobinhoodRefreshTokenResponse {
    private String access_token;
    private float expires_in;
    private String refresh_token;
    public RobinhoodRefreshTokenResponse() {
        super();
    }
    public RobinhoodRefreshTokenResponse(String access_token, String refresh_token, float expires_in) {
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.expires_in = expires_in;
    }
}
