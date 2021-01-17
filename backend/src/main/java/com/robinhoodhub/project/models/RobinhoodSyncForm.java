package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RobinhoodSyncForm {
    private String username;
    private String password;
    private String mfa_code;
}
