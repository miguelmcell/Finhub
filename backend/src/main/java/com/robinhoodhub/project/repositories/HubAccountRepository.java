package com.robinhoodhub.project.repositories;

import com.robinhoodhub.project.models.HubAccount;
import com.robinhoodhub.project.models.HubProfile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface HubAccountRepository extends MongoRepository<HubAccount, String> {
    @Query(value = "{ 'id' : ?0 }", fields = "{ 'username' : 1, 'email' : 1, 'avatar' : 1, 'visibility' : 1, 'robinhoodStatus' : 1, 'robinhoodUsername' : 1, 'minutesUpdatedAgo': 1, 'overallChange': 1,'dailyChange': 1, 'weeklyChange': 1, 'monthlyChange': 1}")
    String findByTheId(String id);
    @Query(value = "{ 'username' : ?0 }", fields = "{ 'username' : 1, 'avatar' : 1, 'visibility' : 1, 'minutesUpdatedAgo': 1, 'overallChange': 1,'dailyChange': 1, 'weeklyChange': 1, 'monthlyChange': 1, 'lastUpdate':1, 'positions':1, 'webullLastUpdate':1, 'webullOverallChange':1,'webullDailyChange':1,'webullWeeklyChange':1,'webullMonthlyChange':1,'webullPositions':1, 'webullStatus':1, 'robinhoodStatus':1}")
    HubProfile findProfileByUsername(String username);
    @Query(value = "{ 'username' : ?0 }", fields = "{ 'username' : 1, 'email' : 1, 'avatar' : 1, 'visibility' : 1, 'robinhoodStatus' : 1, 'robinhoodUsername' : 1, 'friends': 1, 'robinhoodAccessExp': 1, 'password': 1, 'webullStatus': 1, 'webullUsername': 1}")
    HubAccount findByTheUsernameAccount(String username);
    @Query(value = "{ 'username' : ?0 }", fields = "{ 'username' : 1, 'email' : 1, 'avatar' : 1, 'visibility' : 1, 'robinhoodStatus' : 1, 'robinhoodUsername' : 1}")
    String findByTheUsernameNoPass(String username);
    @Query(value = "{ 'username' : ?0 }", fields = "{ 'id' : 1}")
    String findIdByUsername(String username);
    @Query(value = "{ 'username' : ?0 }", fields = "{ 'friends': 1}")
    HubAccount findFriendsByUsername(String username);
    @Query(value = "{ 'username' : {$regex: ?0, $options: 'i'} }", fields="{'username':1, 'avatar':1}")
    List<HubAccount> searchUsernameRegex(String username);
    @Query(value= "{ 'robinhoodAccessTok' : 1  }", fields="{'username':1, 'avatar':1}")
    List<HubAccount> findConnectedAccounts();

    HubAccount findByUsername(String username);
    List<HubAccount> findByRobinhoodAccessTokExists(boolean exists);
    List<HubAccount> findByWebullAccessTokExists(boolean exists);
}
