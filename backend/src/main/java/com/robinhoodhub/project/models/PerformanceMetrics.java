package com.robinhoodhub.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PerformanceMetrics {
    Instant lastUpdate;
    double overall;
    double daily;
    double weekly;
    double monthly;
}
