package com.robinhoodhub.project.controllers;

import com.robinhoodhub.project.models.DiscordModifyBrokerForm;
import com.robinhoodhub.project.models.FinhubSignUpForm;
import com.robinhoodhub.project.models.SignUpForm;
import com.robinhoodhub.project.services.AccountService;
import com.robinhoodhub.project.services.DiscordAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
    @RequestMapping(value="/discord/webull/addAccount", method = RequestMethod.POST)
    public ResponseEntity addWebullAccount(
            @RequestBody DiscordModifyBrokerForm requestForm
    ) {
        // Verify headers exist
        if(requestForm.getDiscordId()==null || requestForm.getEmail()==null)
            return ResponseEntity.badRequest().build();
        return accountService.addWebullAccount(requestForm);
    }
    @RequestMapping(value="/discord/webull/sendMfa", method = RequestMethod.POST)
    public ResponseEntity sendWebullMfa(
            @RequestBody DiscordModifyBrokerForm requestForm
    ) {
        // Verify headers exist
        if(requestForm.getDiscordId()==null)
            return ResponseEntity.badRequest().build();
        ResponseEntity status = accountService.sendWebullMfa(requestForm.getDiscordId());
        if(status.getStatusCode().value()!=200) {
            return status;
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @RequestMapping(value="/getActiveUsersInServer", method = RequestMethod.GET)
    public ResponseEntity getActiveUsersInServer(
            @RequestHeader("guildId") String guildId
    ) {
        // Verify headers exist
        if(guildId==null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(accountService.getActiveUsersInGuild(guildId));
    }
    @RequestMapping(value="/discord/getUser", method = RequestMethod.GET)
    public ResponseEntity getUser(
            @RequestHeader("discordId") String discordId
    ) {
        // Verify headers exist
        if(discordId==null)
            return ResponseEntity.badRequest().build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(accountService.getUser(discordId));
    }
}
