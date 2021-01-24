package com.robinhoodhub.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robinhoodhub.project.models.*;
import com.robinhoodhub.project.repositories.FinhubAccountRepository;
import com.robinhoodhub.project.repositories.RobinhoodServiceRepository;
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
import java.util.List;
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
    @Autowired
    RobinhoodServiceRepository robinhoodServiceRepository;
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

    public List<FinhubAccount> getLeaderboardInGuild (String guildId) {
        ArrayList<FinhubAccount> finhubAccounts = finhubAccountRepository.findAll().stream()
                .filter(finhubAccount -> finhubAccount.getDiscordServerIds().stream()
                .anyMatch(serverId -> serverId.equals(guildId)))
                .collect(Collectors.toCollection(ArrayList::new));

        for (FinhubAccount account: finhubAccounts) {
            account.setId(null);
            account.setDiscordServerIds(null);
            // Filter out inactive brokers
            account.setBrokers(account.getBrokers().stream()
                .filter(broker -> broker.getStatus().equals("active"))
                .collect(Collectors.toCollection(ArrayList::new))
            );
            // strip everything except performance metrics and name
            for (Broker broker: account.getBrokers()) {
                broker.setStatus(null);
                broker.setBrokerUsername(null);
                broker.setBrokerAccessToken(null);
                broker.setBrokerRefreshToken(null);
                broker.setBrokerTokenExpiration(null);
                broker.setBrokerAccountId(null);
            }
        }
        
        return finhubAccounts;
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
            if(response.statusCode()!=200) {
                if (response.body().toString().contains("Incorrect password or username.")) {
                    return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.body());
                }
                return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to syncWebull, MFA token might be invalid");
            }
            /*
            Map to response object and store in database
             */
            ObjectMapper objectMapper = new ObjectMapper();
            WebullSyncResponse webullSyncResponse = objectMapper.readValue(response.body().toString(), WebullSyncResponse.class);

            FinhubAccount finhubAccount = finhubAccountRepository.findByDiscordId(discordId);
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
            webullBroker.setStatus("active");
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

    public ResponseEntity robinhoodUpdatePerformanceHoldings(String discordId) {
        try {
            FinhubAccount finhubAccount = finhubAccountRepository.findByDiscordId(discordId);
            Optional<Broker> robinhoodBrokerOptional = finhubAccount.getBrokers().stream().
                    filter(broker -> broker.getName().equals("robinhood"))
                    .findFirst();
            Broker robinhoodBroker = robinhoodBrokerOptional.orElse(null);
            if (robinhoodBroker==null) {
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("noRobinhoodAccount");
            }
            if (robinhoodBroker.getBrokerAccessToken()==null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("robinhoodHasNotBeenSynced");
            }
            String accessToken = aesUtil.decrypt(robinhoodBroker.getBrokerAccessToken(), s);
            String accountId = robinhoodBroker.getBrokerAccountId();
            
            HttpResponse positionResponse = robinhoodServiceRepository.getPositions(accessToken);
            HttpResponse performanceResponse = robinhoodServiceRepository.getPerformance(accessToken);

            if(positionResponse.statusCode()!=200 && performanceResponse.statusCode()!=200)
                return ResponseEntity.status(positionResponse.statusCode()).body("request error for robinhood position+performance");
            else if (positionResponse.statusCode()!=200)
                return ResponseEntity.status(positionResponse.statusCode()).body("request error for robinhood position");
            else if (performanceResponse.statusCode()!=200)
                return ResponseEntity.status(performanceResponse.statusCode()).body("request error for robinhood performance");
            /*
            Map to response object and store in database
             */
            ObjectMapper objectMapper = new ObjectMapper();
            RobinhoodPositionsResponse robinhoodSyncResponse = objectMapper.readValue(positionResponse.body().toString(), RobinhoodPositionsResponse.class);
            RobinhoodPerformanceResponse robinhoodPerformanceResponse = objectMapper.readValue(performanceResponse.body().toString(), RobinhoodPerformanceResponse.class);


            robinhoodBroker.setPositions(robinhoodSyncResponse.getPositions());
            PerformanceMetrics performanceMetrics = PerformanceMetrics.builder()
                    .overall(robinhoodPerformanceResponse.getOverall())
                    .daily(robinhoodPerformanceResponse.getDaily())
                    .weekly(robinhoodPerformanceResponse.getWeekly())
                    .monthly(robinhoodPerformanceResponse.getMonthly())
                    .lastUpdate(Instant.now())
                    .build();
            robinhoodBroker.setPerformanceMetrics(performanceMetrics);
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

    public ResponseEntity addRobinhoodAccount (DiscordModifyBrokerForm requestForm) {
        FinhubAccount account = getUser(requestForm.getDiscordId());
        if (account==null) {
            return ResponseEntity.badRequest().body("finhub account not found");
        } else if (account.getBrokers().stream()
                .anyMatch(broker -> broker.getName().equals("robinhood"))) {
            return ResponseEntity.badRequest().body("robinhood account already exists");
        } else {
            ArrayList<Broker> curBrokers = account.getBrokers();
            Broker newRobinhoodAccount = Broker.builder()
                    .name("robinhood")
                    .status("inactive")
                    .brokerUsername(requestForm.getUsername())
                    .build();
            curBrokers.add(newRobinhoodAccount);
            account.setBrokers(curBrokers);
            finhubAccountRepository.save(account);
            // connect to broker API
            RobinhoodSyncForm robinhoodSyncForm = RobinhoodSyncForm.builder()
                .username(requestForm.getUsername())
                .password(requestForm.getPassword())
                .mfa_code(requestForm.getMfaCode())
                .build();
            ResponseEntity syncResponse = syncRobinhood(requestForm.getDiscordId(), robinhoodSyncForm);
            if (!syncResponse.getStatusCode().equals(HttpStatus.OK)) {
                // if something goes wrong then delete broker that was trying to be added
                ArrayList<Broker>  brokersRobinhoodExcluded = account.getBrokers().stream().
                    filter(broker -> !broker.getName().equals("robinhood")).collect(Collectors.toCollection(ArrayList::new));
                account.setBrokers(brokersRobinhoodExcluded);
                finhubAccountRepository.save(account);
                return syncResponse;
            }
            // Populate robinhood metrics
            return robinhoodUpdatePerformanceHoldings(requestForm.getDiscordId());
        }
    }

    // populate finhub account with access token for robinhood
    public ResponseEntity syncRobinhood(String discordId, RobinhoodSyncForm syncForm) {
        try {
            HttpResponse response = robinhoodServiceRepository.getAccessToken(syncForm);
            if(response.statusCode()==400) {
                if (response.body().equals("Invalid code")) {
                    System.out.println("syncRobinhood: Invalid code");
                    return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Robinhood MFA code is invalid");
                } else if (response.body().equals("Unable to log in with provided credentials")) {
                    System.out.println("syncRobinhood: Unable to log in with provided credentials");
                    return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unable to log in with provided credentials");
                }
                
            }
            else if(response.statusCode()!=200) {
                return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("syncRobinhood: Failed to get access token from robinhoodServiceRepository");
            }
            System.out.println(response.statusCode());
            /*
            Map to response object and store in database
             */
            ObjectMapper objectMapper = new ObjectMapper();
            RobinhoodSyncResponse robinhoodSyncResponse = objectMapper.readValue(response.body().toString(), RobinhoodSyncResponse.class);

            FinhubAccount finhubAccount = finhubAccountRepository.findByDiscordId(discordId);
            Optional<Broker> robinhoodBrokerOptional = finhubAccount.getBrokers().stream().
                    filter(broker -> broker.getName().equals("robinhood"))
                    .findFirst();
            Broker robinhoodBroker = robinhoodBrokerOptional.orElse(null);
            if (robinhoodBroker==null) {
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("noRobinhoodAccount");
            }
            robinhoodBroker.setBrokerAccessToken(aesUtil.encrypt(robinhoodSyncResponse.getAccess_token(), s));
            Instant curTime = Instant.now();
            robinhoodBroker.setBrokerTokenExpiration(curTime.plusSeconds((long) robinhoodSyncResponse.getExpires_in()).toString());
            robinhoodBroker.setStatus("active");
            finhubAccountRepository.save(finhubAccount);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok(HttpStatus.OK);
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
