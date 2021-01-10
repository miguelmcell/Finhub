package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;

@Builder
@Data
@Document(collection = "finhub_accounts")
public class FinhubAccount {
    @Id
    String id;
    String discordId; // One discord ID per finhub account
    ArrayList<String> discordServerIds;
    ArrayList<Broker> brokers;
}
