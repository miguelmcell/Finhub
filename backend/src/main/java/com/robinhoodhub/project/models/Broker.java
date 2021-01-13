package com.robinhoodhub.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class Broker {
    /**
     * Account Information
     */
    String name;
    String status; // inactive, session expired, active
    String brokerUsername;
    String brokerAccessToken;
    String brokerRefreshToken;
    String brokerTokenExpiration;

    String brokerAccountId;// Required for webull to not query account ID multiple times

    /**
     * Performance Metrics
     */
    PerformanceMetrics performanceMetrics;
    StockPosition[] positions;
}
