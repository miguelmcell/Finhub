package com.robinhoodhub.project.repositories;

import com.robinhoodhub.project.models.RobinhoodSyncForm;
import com.robinhoodhub.project.models.RobinhoodSyncResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Repository
public class RobinhoodServiceRepository {
    public HttpResponse<String> getAccessToken(RobinhoodSyncForm syncForm) throws Exception, IOException, InterruptedException {
        var objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(syncForm);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://my-finhub-broker-service:5003/robinhoodRepository/login"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("content-type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request,
            HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public HttpResponse<String>
    getPositions(String accessToken) throws Exception{
        var objectMapper = new ObjectMapper();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://my-finhub-broker-service:5003/robinhoodRepository/getPositions"))
                .headers("content-type", "application/json","Authorization", "Bearer " + accessToken)
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public HttpResponse<String> getPerformance(String accessToken) throws Exception{
        var objectMapper = new ObjectMapper();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://my-finhub-broker-service:5003/robinhoodRepository/getPerformances"))
                .headers("content-type", "application/json","Authorization", "Bearer " + accessToken)
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public boolean getStatus(){
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://my-finhub-broker-service:5003/robinhoodRepository/status"))
                    .build();

            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            if (response.body().equals(""))
                return true;
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}
