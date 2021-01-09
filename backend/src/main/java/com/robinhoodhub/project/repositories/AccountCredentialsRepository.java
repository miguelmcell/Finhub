package com.robinhoodhub.project.repositories;

import com.robinhoodhub.project.models.HubCredentials;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// change string to char[]?
@Repository
public interface AccountCredentialsRepository extends MongoRepository<HubCredentials, String> {
    HubCredentials findByAssociationId(String associationId);
    void deleteByAssociationId(String associationId);
}
