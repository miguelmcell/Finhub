package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RobinhoodRefreshTokenRquest {
    private String refresh_token;
}
