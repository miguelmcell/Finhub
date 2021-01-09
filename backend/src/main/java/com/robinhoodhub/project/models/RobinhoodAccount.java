package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "robinhood_account")
public class RobinhoodAccount {
    @Id String id;
    String status;
    String username;
    String password;
}
