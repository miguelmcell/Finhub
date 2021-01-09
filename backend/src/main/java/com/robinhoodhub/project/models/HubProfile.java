package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Builder
@Data
public class HubProfile {
    String username;
    String avatar;
    String visibility;
    Float minutesUpdatedAgo;
    Float overallChange;
    Float dailyChange;
    Float weeklyChange;
    Float monthlyChange;
    Instant lastUpdate;
    StockPosition[] positions;

    Instant webullLastUpdate;
    Float webullOverallChange;
    Float webullDailyChange;
    Float webullWeeklyChange;
    Float webullMonthlyChange;
    StockPosition[] webullPositions;
    String webullStatus;
    String robinhoodStatus;

    /* Planed Additions
        Holdings
        Friend request thing
     */
}
