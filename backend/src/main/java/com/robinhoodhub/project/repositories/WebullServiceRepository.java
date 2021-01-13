package com.robinhoodhub.project.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.robinhoodhub.project.models.RobinhoodSyncForm;
import com.robinhoodhub.project.models.WebullSyncForm;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Repository
public class WebullServiceRepository {
    public HttpResponse<String> getAccessToken(WebullSyncForm syncForm) throws Exception, IOException, InterruptedException {
        var objectMapper = new ObjectMapper();
        String requestBody = objectMapper.writeValueAsString(syncForm);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5000/webullRepository/login"))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .header("content-type", "application/json")
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public HttpResponse<String>
    getPositions(String accessToken, String account_id) throws Exception{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5000/webullRepository/getPositions"))
                .headers("content-type", "application/json","access_token", accessToken, "account_id", account_id)
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public HttpResponse<String> sendMfaToken(String email) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5000/webullRepository/getMfa?email="+email))
                .headers("content-type", "application/json","email", email)
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public HttpResponse<String>
    getPerformances(String accessToken, String account_id) throws Exception{
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://127.0.0.1:5000/webullRepository/getPerformances"))
                .headers("content-type", "application/json","access_token", accessToken, "account_id", account_id)
                .build();

        HttpResponse<String> response = client.send(request,
                HttpResponse.BodyHandlers.ofString());

        return response;
    }

    public boolean getStatus(){
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://127.0.0.1:5000/webullRepository/status"))
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
