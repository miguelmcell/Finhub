package com.robinhoodhub.project.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockPosition {
    String stockName;
    String percentage;
    String type;
}
