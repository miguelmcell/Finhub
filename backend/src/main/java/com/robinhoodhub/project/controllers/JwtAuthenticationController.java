package com.robinhoodhub.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robinhoodhub.project.models.JwtRequest;
import com.robinhoodhub.project.models.JwtResponse;
import com.robinhoodhub.project.repositories.HubAccountRepository;
import com.robinhoodhub.project.services.JwtUserDetailsService;
import com.robinhoodhub.project.utils.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@CrossOrigin
public class JwtAuthenticationController {
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private HubAccountRepository hubAccountRepository;

    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);
        String userId = hubAccountRepository.findIdByUsername(authenticationRequest.getUsername());
        //mapper
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = mapper.readValue(userId, Map.class);
        return ResponseEntity.ok(new JwtResponse(token, map.get("_id")));
    }
    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
