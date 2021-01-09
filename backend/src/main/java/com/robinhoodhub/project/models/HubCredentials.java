package com.robinhoodhub.project.models;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Data
@Document(collection = "credentials")
public class HubCredentials {
    @Id String id;
    // account Id for associated password
    String associationId;
    String credential;
}
