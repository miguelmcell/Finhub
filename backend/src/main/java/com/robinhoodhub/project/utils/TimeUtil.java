package com.robinhoodhub.project.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

import com.robinhoodhub.project.models.Broker;

public class TimeUtil {
    private TimeUtil() {}
    static DateTimeFormatter webullDateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss.SSSX");
    public static Boolean isMarketHours() {
        ZonedDateTime curTime = ZonedDateTime.now();
        curTime = curTime.withZoneSameInstant(ZoneOffset.of("-05:00"));
        Calendar cal = Calendar.getInstance();
        cal.setTime(java.util.Date.from(curTime.toInstant()));
        if(cal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY || cal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
            System.out.println("Wont update during weekend");
            return false;
        } else if(cal.get(Calendar.HOUR_OF_DAY)>16 || cal.get(Calendar.HOUR_OF_DAY)<8) {
            System.out.println("Wont update after market hours");
            return false;
        } 
        return true;
    }

    public static LocalDateTime getBrokerExpirationDate(Broker broker) {
        if (broker.getName().equals("robinhood")) {
            return LocalDateTime.parse(broker.getBrokerTokenExpiration(), DateTimeFormatter.ISO_DATE_TIME);
        } else if (broker.getName().equals("webull")) {
            return LocalDateTime.parse(broker.getBrokerTokenExpiration(), webullDateFormatter);
        } else {
            return null;
        }
    }
}
