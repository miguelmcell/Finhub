package com.robinhoodhub.project.schedulers;

import com.robinhoodhub.project.models.Broker;
import com.robinhoodhub.project.models.FinhubAccount;
import com.robinhoodhub.project.models.HubAccount;
import com.robinhoodhub.project.repositories.FinhubAccountRepository;
import com.robinhoodhub.project.repositories.HubAccountRepository;
import com.robinhoodhub.project.repositories.RobinhoodServiceRepository;
import com.robinhoodhub.project.services.AccountService;
import com.robinhoodhub.project.services.DiscordAccountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
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
        ZonedDateTime curTime = ZonedDateTime.now();
        curTime = curTime.withZoneSameInstant(ZoneOffset.of("-05:00"));
        Calendar cal = Calendar.getInstance();
        cal.setTime(java.util.Date.from(curTime.toInstant()));
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            System.out.println("Wont update during weekend");
            return;
        } else if(cal.get(Calendar.HOUR_OF_DAY)>16 || cal.get(Calendar.HOUR_OF_DAY)<8) {
            System.out.println("Wont update after market hours");
            return;
        } 
        System.out.println("Starting batch update process for finhub users at " + fateFormat.format(Date.from(curTime.toInstant())));
        // get list of accounts with active status
        // TODO dont check if any broker is active, filter out brokers that are not active
        List<FinhubAccount> finhubAccounts = finhubAccountRepository.findAll().stream()
            .filter(account -> account.getBrokers().stream()
            .anyMatch(broker -> broker.getStatus().equals("active")))
            .collect(Collectors.toCollection(ArrayList::new));
        DateTimeFormatter formatter =
        DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
        LocalDateTime curDateTime = LocalDateTime.now();
        for (FinhubAccount account : finhubAccounts) {
            System.out.println("Validating expiration dates: " + account.getDiscordId());
            // Check if token still valid, if not then disconnect account and change status
            for (Broker broker : account.getBrokers()) {
                LocalDateTime expirationTime = null;
                if (broker.getName().equals("robinhood")) {
                    expirationTime = LocalDateTime.parse(broker.getBrokerTokenExpiration(), DateTimeFormatter.ISO_DATE_TIME);
                } else if (broker.getName().equals("webull")) {
                    expirationTime = LocalDateTime.parse(broker.getBrokerTokenExpiration(), formatter);
                } else {
                    System.out.println("invalid broker name found");
                    return;
                }
                if (curDateTime.isAfter(expirationTime)) {
                    System.out.println("Session for " + broker.getName() + "has expired");
                    broker.setStatus("inactive");
                    broker.setBrokerAccessToken(null);
                    broker.setBrokerTokenExpiration(null);
                    finhubAccountRepository.save(account);
                } else {
                    if (broker.getName().equals("robinhood")){
                        System.out.println("Robinhood account updated");
                        discordAccountService.robinhoodUpdatePerformanceHoldings(account.getDiscordId());
                    }
                    else if (broker.getName().equals("webull")) {
                        System.out.println("Webull account updated");
                        discordAccountService.webullUpdatePerformanceHoldings(account.getDiscordId());
                    }
                }

            }
        }
    }

    @Scheduled(cron = "0 0 */1 * * ?")//"0 0 */1 * * ?"
    public void reportTime() {
        if(activeProfile.equals("dev")){
            System.out.println("dev will not update");
            return;
        }
        System.out.println("Checking if should update(new and improvedd)");
        ZonedDateTime curTime = ZonedDateTime.now();
        curTime = curTime.withZoneSameInstant(ZoneOffset.of("-05:00"));
        Calendar cal = Calendar.getInstance();
        cal.setTime(Date.from(curTime.toInstant()));
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            System.out.println("Wont update during weekend");
            return;
        } else if(cal.get(Calendar.HOUR_OF_DAY)>20 || cal.get(Calendar.HOUR_OF_DAY)<14) {
            System.out.println("Wont update after market hours");
            return;
        }
        System.out.println("Starting batch update process at " + fateFormat.format(Date.from(curTime.toInstant())));
        List<HubAccount> hubAccounts = hubAccountRepository.findByRobinhoodAccessTokExists(true);
        List<HubAccount> webullHubAccounts = hubAccountRepository.findByWebullAccessTokExists(true);

        for (HubAccount account : hubAccounts) {
            System.out.println("Checking robinhood account: " + account.getUsername());
            if(account.getRobinhoodAccessExp()!=null&&account.getRobinhoodAccessTok()!=null) {
                System.out.println("Updating robinhood");
                // Check if token still valid, if not then disconnect account and change status
                if(curTime.toInstant().isAfter(account.getRobinhoodAccessExp())) {
                    account.setRobinhoodStatus("Session Expired");
                    account.setRobinhoodAccessTok(null);
                    account.setRobinhoodAccessExp(null);
                    hubAccountRepository.save(account);
                } else {
                    accountService.updatePerformanceHoldings(account.getUsername());
                }
            }
        }
        for (HubAccount account : webullHubAccounts) {
            System.out.println("Checking webull account: " + account.getUsername());
            if(account.getWebullAccessExp()!=null&&account.getWebullAccessTok()!=null) {
                // Check if token still valid, if not then disconnect account and change status
               LocalDateTime tempTime = LocalDateTime.parse(account.getWebullAccessExp().split("\\.")[0]);
               Instant newDate = Date.from(tempTime.atZone(ZoneId.systemDefault()).toInstant()).toInstant();
               if(curTime.toInstant().isAfter(newDate)) {
                   account.setWebullStatus("Webull Session Expired");
                   account.setWebullAccessTok(null);
                   account.setWebullAccessExp(null);
                   hubAccountRepository.save(account);
               } else {
                System.out.println("Updating webull");
                accountService.webullUpdatePerformanceHoldings(account.getUsername());
               }
            }
        }
    }
}
