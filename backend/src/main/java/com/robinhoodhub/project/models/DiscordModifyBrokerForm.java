package com.robinhoodhub.project.models;

import lombok.Data;

@Data
public class DiscordModifyBrokerForm {
    private String discordId;
    private String email;

    private String username;
    private String password;
    private String mfaCode;
}
