package com.robinhoodhub.project.controllers;

import com.robinhoodhub.project.models.FinhubSignUpForm;
import com.robinhoodhub.project.models.SignUpForm;
import com.robinhoodhub.project.services.AccountService;
import com.robinhoodhub.project.services.DiscordAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class HubDiscordController {
    @Autowired
    DiscordAccountService accountService;

    @RequestMapping(value="/discord/signUp", method = RequestMethod.POST)
    public ResponseEntity signUp(
            @RequestBody FinhubSignUpForm signUpForm
    ) {
        // Verify headers exist
        if(signUpForm.getDiscordId()==null||signUpForm.getGuildId()==null||signUpForm.getGuildName()==null)
            return ResponseEntity.badRequest().build();
        return accountService.register(signUpForm);
    }
}
