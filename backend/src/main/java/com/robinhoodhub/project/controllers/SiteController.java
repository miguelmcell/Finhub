package com.robinhoodhub.project.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SiteController {
    @RequestMapping("/getHomeInfo")
    public String getHomeInfo() {
        return "";
    }

    @RequestMapping("/queryUser")
    public String queryUser() {
        // include page number?
        return "";
    }

    @RequestMapping("/isAuthenticated")
    public ResponseEntity<HttpStatus> isAuthenticated() {
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
