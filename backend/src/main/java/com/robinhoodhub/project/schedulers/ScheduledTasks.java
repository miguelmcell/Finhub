package com.robinhoodhub.project.schedulers;

import com.robinhoodhub.project.models.Broker;
import com.robinhoodhub.project.models.FinhubAccount;
import com.robinhoodhub.project.repositories.FinhubAccountRepository;
import com.robinhoodhub.project.repositories.HubAccountRepository;
import com.robinhoodhub.project.repositories.RobinhoodServiceRepository;
import com.robinhoodhub.project.services.AccountService;
import com.robinhoodhub.project.services.DiscordAccountService;
import com.robinhoodhub.project.utils.TimeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScheduledTasks {
    @Autowired
    HubAccountRepository hubAccountRepository;
    @Autowired
    FinhubAccountRepository finhubAccountRepository;
    @Autowired
    RobinhoodServiceRepository robinhoodServiceRepository;
    @Autowired
    AccountService accountService;
    @Autowired
    DiscordAccountService discordAccountService;
    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;

    private static final SimpleDateFormat fateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "0 */30 * * * ?")//"0 0 */1 * * ?"  "0 */30 * * * ?"
    public void updateHourlyFinhubAccounts() {
        // if(activeProfile.equals("dev")){
        //     System.out.println("dev will not update");
        //     return;
        // }
        System.out.println("Checking if discord finhub users should update");
        if (TimeUtil.isMarketHours().equals(false)) 
            return;

        LocalDateTime curDateTime = LocalDateTime.now();
        System.out.println("Starting batch update process for finhub users at " + curDateTime);

        // get list of accounts with active status
        List<FinhubAccount> finhubAccounts = finhubAccountRepository.findAll().stream()
            .filter(account -> !account.getBrokers().isEmpty())
            .map(account -> {
                account.setBrokers(account.getBrokers().stream()
                    .filter(broker -> broker.getStatus().equals("active"))
                    .collect(Collectors.toCollection(ArrayList::new)));
                return account;
            }).collect(Collectors.toCollection(ArrayList::new));
        
        for (FinhubAccount account : finhubAccounts) {
            System.out.println("Validating expiration dates for discordId: " + account.getDiscordId());
            // Check if token still valid, if not then disconnect account and change status
            for (Broker broker : account.getBrokers()) {
                LocalDateTime expirationTime = TimeUtil.getBrokerExpirationDate(broker);
                if (expirationTime==null) {
                    System.out.println("invalid broker name found");
                    return;
                }
                
                // set broker to inactive if token expired
                if (curDateTime.isAfter(expirationTime)) {
                    System.out.println("Session for " + broker.getName() + "has expired");
                    broker.setStatus("inactive");
                    broker.setBrokerAccessToken(null);
                    broker.setBrokerTokenExpiration(null);
                    finhubAccountRepository.save(account);
                } else {
                    if (broker.getName().equals("robinhood")){
                        discordAccountService.robinhoodUpdatePerformanceHoldings(account.getDiscordId());
                        System.out.println("Robinhood account updated");
                    }
                    else if (broker.getName().equals("webull")) {
                        discordAccountService.webullUpdatePerformanceHoldings(account.getDiscordId());
                        System.out.println("Webull account updated");
                    }
                }

            }
        }
    }
}
