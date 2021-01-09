package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class WebullSyncForm {
    private String email;
    private String password;
    private String mfa;
}
