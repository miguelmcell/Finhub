package com.robinhoodhub.project.models;

import lombok.Data;

@Data
public class SignUpForm {
    private String username;
    private String email;
    private String password;
    private String passwordConfirmation;
}
