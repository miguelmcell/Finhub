package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LeaderboardUser {
    String discordId;
    
    PerformanceMetrics performanceMetrics;
    StockPosition[] positions;
}
