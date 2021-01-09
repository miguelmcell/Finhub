package com.robinhoodhub.project.schedulers;

import com.robinhoodhub.project.models.HubAccount;
import com.robinhoodhub.project.repositories.HubAccountRepository;
import com.robinhoodhub.project.repositories.RobinhoodServiceRepository;
import com.robinhoodhub.project.services.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.*;
import java.util.Calendar;
import java.util.List;

@Component
public class ScheduledTasks {
    @Autowired
    HubAccountRepository hubAccountRepository;
    @Autowired
    RobinhoodServiceRepository robinhoodServiceRepository;
    @Autowired
    AccountService accountService;
    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;

    private static final SimpleDateFormat fateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
