package com.robinhoodhub.project.services;

import com.robinhoodhub.project.models.*;
import com.robinhoodhub.project.repositories.FinhubAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class DiscordAccountService {
    @Autowired
    FinhubAccountRepository finhubAccountRepository;

    public ResponseEntity register(FinhubSignUpForm signUpForm) {
        /*
         * If new account, create new finhub account and save
         * If account exists, check if given guild is already included, if so return that its already been included,
         * otherwise add user to list of existing guild + new one and save
         */
        switch(doesAccountExist(signUpForm)) {
            case 0:
                ArrayList<String> discordServerList = new ArrayList<>();
                discordServerList.add(signUpForm.getGuildId());
                FinhubAccount newAccount = FinhubAccount.builder()
                        .id(UUID.randomUUID().toString())
                        .discordId(signUpForm.getDiscordId())
                        .discordServerIds(discordServerList)
                        .brokers(new ArrayList<>())
                        .build();
                finhubAccountRepository.save(newAccount);
                return ResponseEntity.ok(HttpStatus.OK);
            case 1:
                FinhubAccount finhubAccount = finhubAccountRepository.findByDiscordId(signUpForm.getDiscordId());
                ArrayList<String> curServerList = finhubAccount.getDiscordServerIds();
                curServerList.add(signUpForm.getGuildId());
                finhubAccount.setDiscordServerIds(curServerList);
                finhubAccountRepository.save(finhubAccount);
                return ResponseEntity.ok().body("Existing user added to new finhub group");
            case 2:
                return ResponseEntity.ok().body("User already in given finhub group");
            default:
                return ResponseEntity.badRequest().build();
        }
    }

    public int doesAccountExist(FinhubSignUpForm signUpForm) {
        /*
          0 - account does not exist
          1 - account exists but given guild has not been added to account
          2 - account exists but given guild has already been added to account
         */
        FinhubAccount finhubAccount = finhubAccountRepository.findByDiscordId(signUpForm.getDiscordId());
        if (finhubAccount == null) {
            return 0;
        }
        boolean isServerAlreadyRegistered = finhubAccount.getDiscordServerIds().stream()
                .anyMatch(id -> id.equals(signUpForm.getGuildId()));
        return isServerAlreadyRegistered ? 2 : 1;
    }
}
