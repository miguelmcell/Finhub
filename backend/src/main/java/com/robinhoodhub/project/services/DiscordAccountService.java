package com.robinhoodhub.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robinhoodhub.project.models.*;
import com.robinhoodhub.project.repositories.FinhubAccountRepository;
import com.robinhoodhub.project.repositories.WebullServiceRepository;
import com.robinhoodhub.project.utils.AESUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DiscordAccountService {
    @Autowired
    FinhubAccountRepository finhubAccountRepository;
    @Autowired
    WebullServiceRepository webullServiceRepository;
    AESUtil aesUtil = new AESUtil();
    @Value("${aes.s}")
    private String s;

    public ArrayList<String> getActiveUsersInGuild (String guildId) {
        return finhubAccountRepository.findAll().stream()
                .filter(finhubAccount -> finhubAccount.getDiscordServerIds().stream()
                .anyMatch(serverId -> serverId.equals(guildId)))
                .map(FinhubAccount::getDiscordId)
                .collect(Collectors.toCollection(ArrayList::new));
    }
    public ResponseEntity sendWebullMfa (String discordId) {
        FinhubAccount account = getUser(discordId);
        if (account==null) {
            return ResponseEntity.badRequest().body("finhub account not found");
        }
        Broker webullAccount = account.getBrokers().stream()
                .filter(broker -> broker.getName().equals("webull"))
                .findFirst()
                .orElse(null);

        if (webullAccount == null) {
            return ResponseEntity.badRequest().body("webull account does not exist");
        } else {
            try {
                webullServiceRepository.sendMfaToken(webullAccount.getBrokerUsername());
            } catch (Exception e) {
                return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
            }
            return ResponseEntity.ok(HttpStatus.OK);
        }
    }

    public ResponseEntity syncWebull(String discordId, WebullSyncForm syncForm) {
        try {
            HttpResponse response = webullServiceRepository.getAccessToken(syncForm);
            if(response.statusCode()!=200)
                return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("webullRepositoryNon2xx");
            /*
            Map to response object and store in database
             */
            ObjectMapper objectMapper = new ObjectMapper();
            WebullSyncResponse webullSyncResponse = objectMapper.readValue(response.body().toString(), WebullSyncResponse.class);

            FinhubAccount finhubAccount = finhubAccountRepository.findByDiscordId(discordId);
            // System.out.println(webullSyncResponse.getAccess_token() +","+webullSyncResponse.getAccount_id()+","+webullSyncResponse.getRefresh_token()+","+webullSyncResponse.getExpirationTime());
            Optional<Broker> webullBrokerOptional = finhubAccount.getBrokers().stream().
                    filter(broker -> broker.getName().equals("webull"))
                    .findFirst();
            Broker webullBroker = webullBrokerOptional.orElse(null);
            if (webullBroker==null) {
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("noWebullAccount");
            }
            webullBroker.setBrokerAccessToken(aesUtil.encrypt(webullSyncResponse.getAccess_token(), s));
            webullBroker.setBrokerAccountId(webullSyncResponse.getAccount_id());
            webullBroker.setBrokerRefreshToken(webullSyncResponse.getRefresh_token());
            webullBroker.setBrokerTokenExpiration(webullSyncResponse.getExpirationTime());
            webullBroker.setStatus("Connected");
            finhubAccountRepository.save(finhubAccount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public ResponseEntity webullUpdatePerformanceHoldings(String discordId) {
        try {
            FinhubAccount finhubAccount = finhubAccountRepository.findByDiscordId(discordId);
            Optional<Broker> webullBrokerOptional = finhubAccount.getBrokers().stream().
                    filter(broker -> broker.getName().equals("webull"))
                    .findFirst();
            Broker webullBroker = webullBrokerOptional.orElse(null);
            if (webullBroker==null) {
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("noWebullAccount");
            }
            if (webullBroker.getBrokerAccessToken()==null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("webullHasNotBeenSynced");
            }
            String accessToken = aesUtil.decrypt(webullBroker.getBrokerAccessToken(), s);
            String accountId = webullBroker.getBrokerAccountId();

            HttpResponse positionResponse = webullServiceRepository.getPositions(accessToken, accountId);
            HttpResponse performanceResponse = webullServiceRepository.getPerformances(accessToken, accountId);

            if(positionResponse.statusCode()!=200 && performanceResponse.statusCode()!=200)
                return ResponseEntity.status(positionResponse.statusCode()).body("request error for webull position+performance");
            else if (positionResponse.statusCode()!=200)
                return ResponseEntity.status(positionResponse.statusCode()).body("request error for webull position");
            else if (performanceResponse.statusCode()!=200)
                return ResponseEntity.status(performanceResponse.statusCode()).body("request error for webull performance");
            /*
            Map to response object and store in database
             */
            ObjectMapper objectMapper = new ObjectMapper();
            RobinhoodPositionsResponse webullSyncResponse = objectMapper.readValue(positionResponse.body().toString(), RobinhoodPositionsResponse.class);
            RobinhoodPerformanceResponse webullPerformanceResponse = objectMapper.readValue(performanceResponse.body().toString(), RobinhoodPerformanceResponse.class);


            webullBroker.setPositions(webullSyncResponse.getPositions());
            PerformanceMetrics performanceMetrics = PerformanceMetrics.builder()
                    .overall(webullPerformanceResponse.getOverall())
                    .daily(webullPerformanceResponse.getDaily())
                    .weekly(webullPerformanceResponse.getWeekly())
                    .monthly(webullPerformanceResponse.getMonthly())
                    .lastUpdate(Instant.now())
                    .build();
            webullBroker.setPerformanceMetrics(performanceMetrics);
            finhubAccountRepository.save(finhubAccount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public ResponseEntity addWebullAccount (DiscordModifyBrokerForm requestForm) {
        FinhubAccount account = getUser(requestForm.getDiscordId());
        if (account==null) {
            return ResponseEntity.badRequest().body("finhub account not found");
        } else if (account.getBrokers().stream()
                .anyMatch(broker -> broker.getName().equals("webull"))) {
            return ResponseEntity.badRequest().body("webull account already exists");
        } else {
            ArrayList<Broker> curBrokers = account.getBrokers();
            Broker newWebullAccount = Broker.builder()
                    .name("webull")
                    .status("inactive")
                    .brokerUsername(requestForm.getEmail())
                    .build();
            curBrokers.add(newWebullAccount);
            account.setBrokers(curBrokers);
            finhubAccountRepository.save(account);
            return ResponseEntity.ok().build();
        }
    }

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
    public int doesAccountExist(String discordId) {
        /*
          0 - account does not exist
          1 - account exists
         */
        FinhubAccount finhubAccount = finhubAccountRepository.findByDiscordId(discordId);
        return finhubAccount == null ? 0 : 1;
    }

    public FinhubAccount getUser(String discordId) {
        return finhubAccountRepository.findByDiscordId(discordId);
    }
}
