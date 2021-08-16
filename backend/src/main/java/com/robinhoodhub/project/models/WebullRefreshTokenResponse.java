package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WebullRefreshTokenResponse {
    private String access_token;
    private String refreshToken;
    private String expirationTime;
    
    public WebullRefreshTokenResponse() {
        super();
    }

    public WebullRefreshTokenResponse(String access_token, String refreshToken, String expirationTime) {
        this.access_token = access_token;
        this.refreshToken = refreshToken;
        this.expirationTime = expirationTime;
    }
}
