package com.robinhoodhub.project.models;

import lombok.Data;

@Data
public class FinhubSignUpForm {
    private String discordId;
    // Should infer guild name from initial request from server and message author
    private String guildId;
    private String guildName;
}
