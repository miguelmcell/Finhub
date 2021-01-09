package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class RobinhoodPositionsResponse {
    StockPosition[] positions;
    public RobinhoodPositionsResponse() {
        super();
    }
    public RobinhoodPositionsResponse(StockPosition[] stockPositions) {
        this.positions = stockPositions;
    }
}
