package com.robinhoodhub.project.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.lang.reflect.Array;
import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@Document(collection = "hub_accounts")
public class HubAccount {
    @Id String id;
    String username;
    String email;
    String avatar;
    String visibility;
    String password; // can be empty
    String robinhoodStatus;
    String robinhoodUsername;// could be null initially
    String webullStatus;
    String webullUsername;
    String webullAccessTok;
    String webullAccessExp;
    String webullRefreshTok;
    String webullAccountId;
    String robinhoodAccessTok;
    Instant robinhoodAccessExp;
    Instant lastUpdate;
    Float overallChange;
    Float dailyChange;
    Float weeklyChange;
    Float monthlyChange;
    Instant webullLastUpdate;
    Float webullOverallChange;
    Float webullDailyChange;
    Float webullWeeklyChange;
    Float webullMonthlyChange;
    StockPosition[] positions;
    StockPosition[] webullPositions;

    String[] friends;
}
