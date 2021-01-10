package com.robinhoodhub.project.repositories;

import com.robinhoodhub.project.models.FinhubAccount;
import com.robinhoodhub.project.models.HubAccount;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinhubAccountRepository extends MongoRepository<FinhubAccount, String> {
    FinhubAccount findByDiscordId(String discordId);
}
