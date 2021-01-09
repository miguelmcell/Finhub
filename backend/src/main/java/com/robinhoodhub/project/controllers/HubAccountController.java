package com.robinhoodhub.project.controllers;

import com.robinhoodhub.project.models.*;
import com.robinhoodhub.project.services.AccountService;
import com.robinhoodhub.project.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class HubAccountController {
    @Autowired
    AccountService accountService;
    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @RequestMapping(value="/getProfile/{username}", method= RequestMethod.GET)
    public HubProfile getProfile(@PathVariable String username){
        // @todo needs to check if public or if friend is checking
        return accountService.getProfile(username);
    }

    @RequestMapping(value="/getAccount", method=RequestMethod.GET)
    public HubAccount getAccount(@RequestHeader("Authorization") String authToken) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));

        return accountService.getAccount(username);
    }

    @RequestMapping(value="/search/{searchTerm}", method=RequestMethod.GET)
    public List<FriendResponseModel> searchFor(@PathVariable String searchTerm) {
        return accountService.searchFor(searchTerm);
    }

    @RequestMapping(value="/getFriends/{id}", method=RequestMethod.GET)
    public List getFriends(@RequestHeader("Authorization") String authToken) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));

        return accountService.getFriends(username);
    }

    @RequestMapping(value="/isFriend", method=RequestMethod.GET)
    public Boolean isFriend(@RequestHeader("Authorization") String authToken, @RequestParam String friendUsername) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        return accountService.isFriend(username, friendUsername);
    }

    @RequestMapping(value="/followUser", method=RequestMethod.POST)
    public ResponseEntity<HttpStatus> followUser(@RequestHeader("Authorization") String authToken, @RequestBody FollowRequestForm followRequestForm) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        accountService.followUser(username, followRequestForm);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping(value="/changePassword")
    public ResponseEntity<HttpStatus> changePassword(
            @RequestHeader("Authorization") String authToken,
            @RequestBody ChangePasswordForm changePasswordForm
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));

        accountService.changePassword(username, changePasswordForm.getPassword());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping("/changeEmail")
    public ResponseEntity<HttpStatus> changeEmail(
            @RequestHeader("Authorization") String authToken,
            @RequestBody ChangeEmailForm changeEmailForm
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        // @todo login required
        accountService.changeEmail(username, changeEmailForm.getEmail());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping("/changeRobinhoodUsername")
    public ResponseEntity<HttpStatus> changeRobinhoodUsername(
            @RequestHeader("Authorization") String authToken,
            @RequestBody ChangeEmailForm changeEmailForm
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        accountService.changeRobinhoodUsername(changeEmailForm.getEmail(), username);
        return ResponseEntity.ok(HttpStatus.OK);
    }
    @PutMapping("/changeWebullUsername")
    public ResponseEntity<HttpStatus> changeWebullUsername(
            @RequestHeader("Authorization") String authToken,
            @RequestBody ChangeEmailForm changeEmailForm
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        accountService.changeWebullUsername(changeEmailForm.getEmail(), username);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping("/changeAvatar")
    public ResponseEntity<HttpStatus> changeAvatar(
            @RequestHeader("Authorization") String authToken,
            @RequestBody ChangeAvatarForm changeAvatarForm
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        accountService.changeAvatar(username,changeAvatarForm.getAvatar());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PutMapping("/changeVisibility")
    public ResponseEntity<HttpStatus> changeVisibility(
            @RequestHeader("Authorization") String authToken,
            @RequestBody ChangeVisibilityForm changeVisibilityForm
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        accountService.changeVisibility(username, changeVisibilityForm.getVisibility());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping(value="/signUp")
    public ResponseEntity signUp(
            @RequestBody SignUpForm signUpForm
    ) {
        // Verify headers exist
        if(signUpForm.getPasswordConfirmation()==null||signUpForm.getPassword()==null||signUpForm.getEmail()==null||signUpForm.getUsername()==null)
            return ResponseEntity.badRequest().build();
        return accountService.registerNewAccount(signUpForm);
    }

    @PostMapping(value="/syncRobinhood")
    public ResponseEntity<HttpStatus> syncRobinhood(
            @RequestHeader("Authorization") String authToken,
            @RequestBody RobinhoodSyncForm syncForm
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        ResponseEntity syncStatus = accountService.syncRobinhood(username, syncForm);
        if(syncStatus.getStatusCode().value()!=200) {
            return syncStatus;
        }
        // continue updating information
        try {
            accountService.updatePerformanceHoldings(username);
        } catch (Exception e) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping(value="/syncWebull")
    public ResponseEntity<HttpStatus> syncWebull(
            @RequestHeader("Authorization") String authToken,
            @RequestBody WebullSyncForm syncForm
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        ResponseEntity syncStatus = accountService.syncWebull(username, syncForm);
        if(syncStatus.getStatusCode().value()!=200) {
            return syncStatus;
        }
        // continue updating information
        try {
            accountService.webullUpdatePerformanceHoldings(username);
        } catch (Exception e) {
            return ResponseEntity.ok(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping(value="/getWebullMfa")
    public ResponseEntity<HttpStatus> getWebullMfa(
            @RequestHeader("Authorization") String authToken
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        ResponseEntity status = accountService.getWebullMfa(username);
        if(status.getStatusCode().value()!=200) {
            return status;
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping(value="/updatePerformanceHoldings")
    public ResponseEntity<HttpStatus> updatePerformanceHoldings(
            @RequestHeader("Authorization") String authToken
    ) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        // Will return 200 status when account doesnt included credentials
        ResponseEntity webullStatus = accountService.webullUpdatePerformanceHoldings(username);
        ResponseEntity robinhoodStatus = accountService.updatePerformanceHoldings(username);
        if(webullStatus.getStatusCode().value()!=200) {
            return webullStatus;
        }
        if(robinhoodStatus.getStatusCode().value()!=200) {
            return robinhoodStatus;
        }
        return ResponseEntity.ok(HttpStatus.OK);
    }

//    @PostMapping(value="/updateWebullPerformanceHoldings")
//    public ResponseEntity<HttpStatus> updateWebullPerformanceHoldings(
//            @RequestHeader("Authorization") String authToken
//    ) {
//        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
//        ResponseEntity status = accountService.webullUpdatePerformanceHoldings(username);
//        if(status.getStatusCode().value()!=200) {
//            return status;
//        }
//        return ResponseEntity.ok(HttpStatus.OK);
//    }

    @PostMapping(value="/disconnectRobinhood")
    public ResponseEntity<HttpStatus> removeRobinhoodAccount(@RequestHeader("Authorization") String authToken){
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        return accountService.removeRobinhoodAccount(username);
    }
    @PostMapping(value="/disconnectWebull")
    public ResponseEntity<HttpStatus> removeWebullAccount(@RequestHeader("Authorization") String authToken){
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        return accountService.removeWebullAccount(username);
    }

    @PostMapping("/deleteAccount")
    public ResponseEntity<HttpStatus> deleteAccount(@RequestHeader("Authorization") String authToken) {
        String username = jwtTokenUtil.getUsernameFromToken(authToken.substring(7));
        accountService.deleteAccount(username);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @RequestMapping("/testStatus")
    public ResponseEntity<HttpStatus> testStatus() {
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @RequestMapping("/testRobinhoodStatus")
    public boolean testRobinhoodStatus() {
        return accountService.testRobinhoodStatus();
    }
    @RequestMapping("/testWebullStatus")
    public boolean testWebullStatus() {
        return accountService.testWebullStatus();
    }
}
