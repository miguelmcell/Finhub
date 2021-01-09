package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
public class RobinhoodSyncForm {
    private String username;
    private String password;
    private String mfa_code;
}
