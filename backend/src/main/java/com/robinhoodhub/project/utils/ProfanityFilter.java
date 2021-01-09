package com.robinhoodhub.project.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import com.robinhoodhub.project.grawlox.Grawlox;

import javax.annotation.PostConstruct;
import java.io.File;

import java.io.IOException;

@Component
public class ProfanityFilter {
    Grawlox grawlox;

    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;


    public ProfanityFilter() throws IOException {
    }

    @PostConstruct
    private void postConstruct() throws IOException {
        if(!activeProfile.equals("dev")){
            grawlox = Grawlox.createFromSwearWordsPath(new File("src/main/resources/sw.txt"));
        }
    }

    public boolean isProfanity(String word) {
        if(!activeProfile.equals("dev")){
            return grawlox.isProfane(word);
        } else {
            return false;
        }
    }
}
