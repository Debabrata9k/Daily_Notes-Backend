package com.daily.note.save.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;

import java.util.List;

import sibApi.ApiClient;
import sibApi.Configuration;
import sibApi.TransactionalEmailsApi;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Value("${brevo.api.key}")
    private String apiKey;

    @Async
    public void sendOtp(String to, String otp) {
        try {
            ApiClient client = Configuration.getDefaultApiClient();
            client.setApiKey(apiKey);

            TransactionalEmailsApi api = new TransactionalEmailsApi();

            SendSmtpEmail email = new SendSmtpEmail();

            email.setSubject("OTP Verification");

            email.setHtmlContent("<h2>Your OTP: " + otp + "</h2>");

            email.setSender(new SendSmtpEmailSender()
                    .email("your_verified_email@example.com"));

            email.setTo(List.of(new SendSmtpEmailTo().email(to)));

            api.sendTransacEmail(email);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}