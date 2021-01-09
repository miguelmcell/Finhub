package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RobinhoodPerformancePositionsResponse {
    private RobinhoodPerformanceResponse robinhoodPerformanceResponse;
    private RobinhoodPositionsResponse robinhoodPositionsResponse;
    public RobinhoodPerformancePositionsResponse(){
        super();
    }
    public RobinhoodPerformancePositionsResponse(RobinhoodPerformanceResponse robinhoodPerformanceResponse, RobinhoodPositionsResponse robinhoodPositionsResponse){
        this.robinhoodPerformanceResponse = robinhoodPerformanceResponse;
        this.robinhoodPositionsResponse = robinhoodPositionsResponse;
    }
}
