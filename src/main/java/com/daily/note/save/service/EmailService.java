package com.daily.note.save.service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Value("${brevo.api.key}")
    private String apiKey;

    @Async
    public void sendOtp(String to, String otp) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            String url = "https://api.brevo.com/v3/smtp/email";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            String body = """
            {
              "sender": {"name": "Daily Notes", "email": "notesdaili@gmail.com"},
              "to": [{"email": "%s"}],
              "subject": "OTP Verification",
              "htmlContent": "<h2>Your OTP: %s</h2><p>Valid for 5 minutes</p>"
            }
            """.formatted(to, otp);

            HttpEntity<String> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(url, request, String.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}