package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RobinhoodPerformanceResponse {
    Float overall;
    Float daily;
    Float weekly;
    Float monthly;
    public RobinhoodPerformanceResponse() {
        super();
    }
    public RobinhoodPerformanceResponse(float overall, float daily, float weekly, float monthly) {
        this.overall = overall;
        this.daily = daily;
        this.weekly = weekly;
        this.monthly = monthly;
    }
}
