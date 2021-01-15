package com.robinhoodhub.project.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robinhoodhub.project.models.*;
import com.robinhoodhub.project.repositories.HubAccountRepository;
import com.robinhoodhub.project.repositories.RobinhoodServiceRepository;
import com.robinhoodhub.project.repositories.WebullServiceRepository;
import com.robinhoodhub.project.utils.AESUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpResponse;
import java.text.Collator;
import java.time.Instant;
import java.util.*;

@Service
public class AccountService {
    @Autowired
    private PasswordEncoder bcryptEncoder;
    @Autowired
    HubAccountRepository hubAccountRepository;
    @Autowired
    RobinhoodServiceRepository robinhoodServiceRepository;
    @Autowired
    WebullServiceRepository webullServiceRepository;
    AESUtil aesUtil = new AESUtil();
    @Value("${aes.s}")
    private String s;

    public boolean doesUsernameExists(String username) {
        return hubAccountRepository.findIdByUsername(username) != null;
    }
    public ResponseEntity registerNewAccount(SignUpForm signUpForm) {
        if(!signUpForm.getPassword().equals(signUpForm.getPasswordConfirmation())) {
            return ResponseEntity.badRequest().body("Incorrect confirmation password");
        }
        // check if username already exists
        if (doesUsernameExists(signUpForm.getUsername())){
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (signUpForm.getUsername().length()>15){
            return ResponseEntity.badRequest().body("Username must be less than 15 characters");
        }
        if (signUpForm.getUsername().length()<5){
            return ResponseEntity.badRequest().body("Username must be longer than 4 characters");
        }
        if(signUpForm.getUsername().contains(" ")){
            return ResponseEntity.badRequest().body("Username cannot contain spaces");
        }
        if(!signUpForm.getUsername().contains("-beta")){
            return ResponseEntity.badRequest().body("Not Ready Yet \uD83D\uDE09");
        }

        HubAccount newAccount = HubAccount.builder()
                .id(UUID.randomUUID().toString())
                .email(signUpForm.getEmail())
                .username(signUpForm.getUsername())
                .password(bcryptEncoder.encode(signUpForm.getPassword()))
                .avatar("baby")
                .visibility("public")
                .robinhoodStatus("Disconnected")
                .webullStatus("Disconnected")
                .friends(null)
                .build();
        hubAccountRepository.save(newAccount);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public HubProfile getProfile(String username) {
        HubProfile profile;
        try {
            profile = hubAccountRepository.findProfileByUsername(username);
            if(profile==null)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist");
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }

        return profile;
    }
    public List<FriendResponseModel> searchFor(String searchTerm) {
        // @todo make search terms at least 5 characters
        List<HubAccount> foundUsers = hubAccountRepository.searchUsernameRegex(searchTerm);
        ArrayList<FriendResponseModel> response = new ArrayList<>();
        foundUsers.stream().forEach(user -> response.add(FriendResponseModel.builder()
                .username(user.getUsername())
                .avatar(user.getAvatar())
                .build()
        ));
        return response;
    }

    public boolean testRobinhoodStatus() {
        return robinhoodServiceRepository.getStatus();
    }

    public boolean testWebullStatus() {
        return webullServiceRepository.getStatus();
    }

    public List<FriendResponseModel> getFriends(String username) {
        Pageable pageable = PageRequest.of(0,3);

        ArrayList<FriendResponseModel> response = new ArrayList<>();
        HubAccount hubAccount = hubAccountRepository.findFriendsByUsername(username);
        List<String> friends = (hubAccount.getFriends()==null)?Collections.emptyList():Arrays.asList(hubAccount.getFriends());
//        PagedListHolder pagedListHolder = new PagedListHolder(friends);
//        pagedListHolder.setPageSize(4);
//        pagedListHolder.setPage(page);
//          pagedListHolder.getPageList()
        for(Object friendName: friends) {
            HubProfile friendProfile=null;
            try {
                friendProfile = getProfile((String)friendName);
            } catch (Exception e) {
                if(e.getCause().getMessage().equals("400 BAD_REQUEST \"Account with given ID does not exist\"")){
                    System.out.println("we gonna delete the friend");
                    ArrayList<String> tempFriendList = new ArrayList<>();
                    for(String friendUsername: hubAccount.getFriends()) {
                        if(!friendUsername.equals((String)friendName)){
                            tempFriendList.add(friendUsername);
                        }
                    }
                    String[] newFriends = new String[hubAccount.getFriends().length-1];
                    HubAccount realAcc = hubAccountRepository.findByUsername(username);
                    realAcc.setFriends(tempFriendList.toArray(newFriends));
                    hubAccountRepository.save(realAcc);
                }
            }
            if(friendProfile!=null) {
                FriendResponseModel tempModel = FriendResponseModel.builder()
                        .avatar(friendProfile.getAvatar())
                        .username((String)friendName)
                        .build();
                response.add(tempModel);
            }
        }

        return response;
    }

    public boolean isFriend(String username, String friendUsername) {
        HubAccount userAccount = getAccount(username);
        if(userAccount.getFriends()==null)
            return false;
        List<String> userFriends = Arrays.asList(userAccount.getFriends());
        for (String friend: userFriends) {
            if(friend.equals(friendUsername)) {
                return true;
            }
        }
        return false;
    }

    public void followUser(String username, FollowRequestForm followRequestForm) {
        HubAccount account;
        try {
            account = hubAccountRepository.findByUsername(username);
            String[] curFriends = account.getFriends();
            if(curFriends==null) {
                curFriends = new String[0];
            }
            if(!followRequestForm.unfollow) { //follow user
                curFriends = ArrayUtils.add(curFriends,followRequestForm.target);
                Arrays.sort(curFriends, Collator.getInstance());
                account.setFriends(curFriends);
                hubAccountRepository.save(account);
            } else {
                List<String> list = Arrays.asList(curFriends);
                int counter = 0;
                for(String friend: list) {
                    if(friend.equals(followRequestForm.target)) {
                        curFriends = ArrayUtils.remove(curFriends, counter);
                        account.setFriends(curFriends);
                        hubAccountRepository.save(account);
                        return;
                    }
                    counter++;
                }
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }
    }

    public HubAccount getAccount(String username) {
        HubAccount result =null;

        try {
            result = hubAccountRepository.findByTheUsernameAccount(username);
            if(result.getRobinhoodAccessExp()!=null) {
                // Check if token has expired
                if(Instant.now().isAfter(result.getRobinhoodAccessExp())) {
                    // Token has expired, set status to Expired and disconnect robinhood account
                    result.setRobinhoodStatus("Session Expired");
                    result.setRobinhoodAccessTok(null);
                    result.setRobinhoodAccessExp(null);
                    hubAccountRepository.save(result);
                    result.setPassword(null);
                    return result;
                }

            }
            result.setPassword(null);
            return result;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }
    }

    // probably change to char[]
    public void changePassword(String userId, String password) {
        HubAccount hubAccount;
        try {
            hubAccount = hubAccountRepository.findByUsername(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }
        hubAccount.setPassword(bcryptEncoder.encode(password));
        hubAccountRepository.save(hubAccount);
    }

    public void changeEmail(String userId, String email) {
        HubAccount hubAccount;

        try {
            hubAccount = hubAccountRepository.findByUsername(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }
        hubAccount.setEmail(email);
        hubAccountRepository.save(hubAccount);
    }

    public void changeRobinhoodUsername(String username, String userId) {
        HubAccount hubAccount;

        try {
            hubAccount = hubAccountRepository.findByUsername(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }
        hubAccount.setRobinhoodUsername(username);
        hubAccountRepository.save(hubAccount);
    }

    public void changeWebullUsername(String username, String userId) {
        HubAccount hubAccount;

        try {
            hubAccount = hubAccountRepository.findByUsername(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }
        hubAccount.setWebullUsername(username);
        hubAccountRepository.save(hubAccount);
    }

    public void changeAvatar(String userId, String avatar) {
        HubAccount hubAccount;
        try {
            hubAccount = hubAccountRepository.findByUsername(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }
        hubAccount.setAvatar(avatar);
        hubAccountRepository.save(hubAccount);
    }

    public void changeVisibility(String userId, String visibility) {
        HubAccount hubAccount;
        try {
            hubAccount = hubAccountRepository.findByUsername(userId);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }
        hubAccount.setVisibility(visibility);
        hubAccountRepository.save(hubAccount);
    }

    public void deleteAccount(String userId) {
        // delete account and credentials page
        try {
            System.out.println("deleting account with userId: "+ userId);
            String accountId = hubAccountRepository.findIdByUsername(userId);
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(accountId, Map.class);
            hubAccountRepository.deleteById(map.get("_id"));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Account with given ID does not exist", e);
        }
    }

    public ResponseEntity<HttpStatus> syncRobinhood(String username, RobinhoodSyncForm syncForm) {
        try {
            HttpResponse response = robinhoodServiceRepository.getAccessToken(syncForm);
            if(response.statusCode()!=200)
                return ResponseEntity.ok(Objects.requireNonNull(HttpStatus.resolve(response.statusCode())));
            /*
            Map to response object and store in database
             */
            ObjectMapper objectMapper = new ObjectMapper();
            RobinhoodSyncResponse robinhoodSyncResponse = objectMapper.readValue(response.body().toString(), RobinhoodSyncResponse.class);
            HubAccount hubAccount = hubAccountRepository.findByUsername(username);
            System.out.println(robinhoodSyncResponse.getAccess_token());
            hubAccount.setRobinhoodAccessTok(aesUtil.encrypt(robinhoodSyncResponse.getAccess_token(), s));
//            System.out.println(aesUtil.decrypt(hubAccount.getRobinhoodAccessTok(),s));

            Instant curTime = Instant.now();
            // Date/time of expiration for token
            hubAccount.setRobinhoodAccessExp(curTime.plusSeconds((long) robinhoodSyncResponse.getExpires_in()));
            hubAccount.setRobinhoodStatus("Connected");
            hubAccount.setLastUpdate(curTime);
            hubAccountRepository.save(hubAccount);
        } catch (Exception e) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> syncWebull(String username, WebullSyncForm syncForm) {
        try {
            syncForm.setEmail(getAccount(username).getWebullUsername());
            HttpResponse response = webullServiceRepository.getAccessToken(syncForm);
            if(response.statusCode()!=200)
                return ResponseEntity.ok(Objects.requireNonNull(HttpStatus.resolve(response.statusCode())));
            /*
            Map to response object and store in database
             */
            ObjectMapper objectMapper = new ObjectMapper();
            WebullSyncResponse webullSyncResponse = objectMapper.readValue(response.body().toString(), WebullSyncResponse.class);
            HubAccount hubAccount = hubAccountRepository.findByUsername(username);
            System.out.println(webullSyncResponse.getAccess_token() +","+webullSyncResponse.getAccount_id()+","+webullSyncResponse.getRefresh_token()+","+webullSyncResponse.getExpirationTime());
            hubAccount.setWebullAccessTok(aesUtil.encrypt(webullSyncResponse.getAccess_token(), s));
            hubAccount.setWebullAccountId(webullSyncResponse.getAccount_id());
            hubAccount.setWebullRefreshTok(webullSyncResponse.getRefresh_token());
            hubAccount.setWebullAccessExp(webullSyncResponse.getExpirationTime());
            hubAccount.setWebullStatus("Connected");
            Instant curTime = Instant.now();
            hubAccount.setLastUpdate(curTime);
            hubAccountRepository.save(hubAccount);
        } catch (Exception e) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> getWebullMfa(String username) {
        try {
            HttpResponse response = webullServiceRepository.sendMfaToken(getAccount(username).getWebullUsername());
        } catch (Exception e) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> updatePerformanceHoldings(String username) {
        try {
            String accessToken = aesUtil.decrypt(hubAccountRepository.findByUsername(username).getRobinhoodAccessTok(), s);
            HubAccount hubAccount = hubAccountRepository.findByUsername(username);
            if(hubAccount.getRobinhoodAccessTok()==null || hubAccount.getRobinhoodStatus().equals("Disconnected")) {
                return ResponseEntity.ok().build();
            }
            HttpResponse positionResponse = robinhoodServiceRepository.getPositions(accessToken);
            HttpResponse performanceResponse = robinhoodServiceRepository.getPerformance(accessToken);
            if(positionResponse.statusCode()!=200 || performanceResponse.statusCode()!=200)
                return ResponseEntity.ok(Objects.requireNonNull(HttpStatus.resolve(positionResponse.statusCode())));
            /*
            Map to response object and store in database
             */
            ObjectMapper objectMapper = new ObjectMapper();
            RobinhoodPositionsResponse robinhoodSyncResponse = objectMapper.readValue(positionResponse.body().toString(), RobinhoodPositionsResponse.class);
            RobinhoodPerformanceResponse robinhoodPerformanceResponse = objectMapper.readValue(performanceResponse.body().toString(), RobinhoodPerformanceResponse.class);


            hubAccount.setPositions(robinhoodSyncResponse.getPositions());
            hubAccount.setOverallChange(robinhoodPerformanceResponse.getOverall());
            hubAccount.setDailyChange(robinhoodPerformanceResponse.getDaily());
            hubAccount.setWeeklyChange(robinhoodPerformanceResponse.getWeekly());
            hubAccount.setMonthlyChange(robinhoodPerformanceResponse.getMonthly());
            hubAccount.setLastUpdate(Instant.now());
            hubAccountRepository.save(hubAccount);
        } catch (Exception e) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> webullUpdatePerformanceHoldings(String username) {
        try {
            HubAccount hubAccount = hubAccountRepository.findByUsername(username);
            if(hubAccount.getWebullAccessTok()==null) {
                return ResponseEntity.ok().build();
            }
            String accessToken = aesUtil.decrypt(hubAccount.getWebullAccessTok(), s);
            String accountId = hubAccount.getWebullAccountId();

            HttpResponse positionResponse = webullServiceRepository.getPositions(accessToken, accountId);
            HttpResponse performanceResponse = webullServiceRepository.getPerformances(accessToken, accountId);
            if(positionResponse.statusCode()!=200 || performanceResponse.statusCode()!=200)
                return ResponseEntity.ok(Objects.requireNonNull(HttpStatus.resolve(positionResponse.statusCode())));
            /*
            Map to response object and store in database
             */
            ObjectMapper objectMapper = new ObjectMapper();
            RobinhoodPositionsResponse webullSyncResponse = objectMapper.readValue(positionResponse.body().toString(), RobinhoodPositionsResponse.class);
            RobinhoodPerformanceResponse webullPerformanceResponse = objectMapper.readValue(performanceResponse.body().toString(), RobinhoodPerformanceResponse.class);


            hubAccount.setWebullPositions(webullSyncResponse.getPositions());
            hubAccount.setWebullOverallChange(webullPerformanceResponse.getOverall());
            hubAccount.setWebullDailyChange(webullPerformanceResponse.getDaily());
            hubAccount.setWebullWeeklyChange(webullPerformanceResponse.getWeekly());
            hubAccount.setWebullMonthlyChange(webullPerformanceResponse.getMonthly());
            hubAccount.setWebullLastUpdate(Instant.now());
            hubAccountRepository.save(hubAccount);
        } catch (Exception e) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> removeRobinhoodAccount(String username) {
        try {
            HubAccount hubAccount = hubAccountRepository.findByUsername(username);
            if (hubAccount==null)
                throw new Exception();
            hubAccount.setRobinhoodAccessTok(null);
            hubAccount.setRobinhoodAccessExp(null);
            hubAccount.setLastUpdate(null);
            hubAccount.setRobinhoodStatus("Disconnected");
            hubAccount.setLastUpdate(null);
            hubAccountRepository.save(hubAccount);
        } catch (Exception e) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    public ResponseEntity<HttpStatus> removeWebullAccount(String username) {
        try {
            HubAccount hubAccount = hubAccountRepository.findByUsername(username);
            if (hubAccount==null)
                throw new Exception();
            hubAccount.setWebullRefreshTok(null);
            hubAccount.setWebullAccessExp(null);
            hubAccount.setWebullAccessTok(null);
            hubAccount.setWebullAccountId(null);
            hubAccount.setWebullLastUpdate(null);
            hubAccount.setWebullStatus("Disconnected");
            hubAccountRepository.save(hubAccount);
        } catch (Exception e) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
