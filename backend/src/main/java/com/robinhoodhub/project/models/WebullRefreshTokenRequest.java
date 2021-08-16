package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebullRefreshTokenRequest {
    private String accessToken;
    private String refreshToken;
}
