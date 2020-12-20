package com.example.mail.service;

import com.example.mail.controller.ExternalMailConfiguration;
import com.example.mail.controller.ExternalMailRequest;
import com.example.mail.controller.LoginInfo;
import com.example.mail.controller.SendMailRequest;
import lombok.Builder;
import lombok.Data;
import lombok.extern.java.Log;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
@Builder
@Service
@Data

public class MailService {

    private final RestTemplate restTemplate;
    private final ExternalMailConfiguration externalMailConfiguration;

    //constructor
    public MailService(RestTemplate restTemplate, ExternalMailConfiguration externalMailConfiguration) {
        this.restTemplate = restTemplate;
        this.externalMailConfiguration = externalMailConfiguration;
    }

    //login-done
    public static UUID getLoginInfo(LoginInfo loginInfo) {
        Map.Entry<LoginInfo, UUID> user = Users.userMap.entrySet().stream()
                .filter(e -> loginInfo.getUsername().equals(e.getKey().getUsername()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("No user"));
        if(loginInfo.getPassword().equals(user.getKey().getPassword())) {
            return(user.getValue());
        } else {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
    }

    public Object sendEmail(SendMailRequest sendMailRequest) throws HttpClientErrorException {
        LoginInfo from = validateFrom(sendMailRequest.getFrom());

        //go through usermap and find the to's full object
        try {
            LoginInfo to = Users.userMap.entrySet().stream()
                    .filter(e -> sendMailRequest.getTo().equals(e.getKey().getUsername()))
                    .findFirst()
                    .orElseThrow(() -> new NullPointerException("No user"))
                    .getKey();

            //add the to, from, and message to the receiver's inbox
            to.getInbox().add(
                    SendMailRequest.builder()
                            .from(sendMailRequest.getFrom())
                            //.to(to.getUsername())
                            .to(sendMailRequest.getTo())
                            .message(sendMailRequest.getMessage())
                            .build());

            //add the to, from, and message to the sender's outbox
            from.getOutbox().add(
                    SendMailRequest.builder()
                    .from(sendMailRequest.getFrom())
                    //.to(to.getUsername())
                            .to(sendMailRequest.getTo())
                    .message(sendMailRequest.getMessage())
                    .build());
            return HttpStatus.OK;

        } catch (Exception e) {
            System.out.println(e);
            return HttpStatus.BAD_REQUEST;
        }
    }

    public LoginInfo validateFrom(UUID from) {
        LoginInfo user;
        try {
            //go through the hashmap and find the first entry that matches the uuid from in the parameter
            user = Users.userMap.entrySet().stream()
                    .filter(e -> from.equals(e.getValue()))
                    .findFirst()
                    .orElseThrow(() -> new NullPointerException("No user"))
                    .getKey();
        } catch (Exception e) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
        if (user == null) {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
        return user;
    }

    public ArrayList<Email> returnInbox(String primaryKey) {
        LoginInfo user = Users.userMap.entrySet().stream()
                .filter(e -> primaryKey.equals(e.getValue().toString()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("No user"))
                .getKey();

        ArrayList<Email> receivedMessages = new ArrayList<Email>();
        //iterate through inbox
        ArrayList<SendMailRequest> inbox = user.getInbox();

        for (SendMailRequest msg : inbox) {
            UUID from = msg.getFrom();
            String sender = Users.userMap.entrySet().stream()
                    .filter(e -> from.equals(e.getValue()))
                    .findFirst()
                    .orElseThrow(() -> new NullPointerException("No user"))
                    .getKey()
                    .getUsername();
            receivedMessages.add(new Email("from: " + sender, "message: " +
                    msg.getMessage()));
        }
        return receivedMessages;
    }

    public ArrayList<Email> returnOutbox(String primaryKey) {
        LoginInfo user = Users.userMap.entrySet().stream()
                .filter(e -> primaryKey.equals(e.getValue().toString()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("No user"))
                .getKey();

        ArrayList<Email> sentMessages = new ArrayList<Email>();
        //iterate through outbox
        ArrayList<SendMailRequest> outbox = user.getOutbox();
        for (SendMailRequest msg : outbox) {
            sentMessages.add(new Email("to: " + msg.getTo(), "message: " +
                    msg.getMessage()));
        }
        return sentMessages;
    }

    public Object receiveEmail(ExternalMailRequest externalMailRequest, String keyValue) throws HttpClientErrorException {
        LoginInfo from = validateFrom(externalMailRequest.getFrom());
        String headerValue = new String(Base64.getEncoder().encode(externalMailConfiguration.getKey().getBytes()));
        HttpHeaders headers = new HttpHeaders();
        headers.add("api-key", headerValue);
        ExternalMailRequest body = ExternalMailRequest.builder()
                .from(externalMailRequest.getFrom())
                .to(externalMailRequest.getTo())
                .message(externalMailRequest.getMessage())
                .build();
        HttpEntity<ExternalMailRequest> httpEntity = new HttpEntity<>(body, headers);
        ResponseEntity<Void> response = restTemplate.exchange("https://" + externalMailConfiguration.getUrl() +
                "/api/v1/email/receiveExternalMail", HttpMethod.POST, httpEntity, Void.class);
        if(response.getStatusCode() != HttpStatus.OK) {
            throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
        }
        try {
            //need to fill in
        } catch (Exception e) {
            System.out.println(e);
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }
}
/*Return 200 (ok) if the "to" user exists and we saved the email successfully to the inbox.
  Return 400 (bad request) if the "to" user does not exist
  There should be a feature switch on this guy as well, and you should return a 503 service unavailable if the endpoint
  is turned off.*/


//            ExternalMailRequest body = ExternalMailRequest.builder()
//                    .from(from)
//                    .to(sendMailRequest.getTo())
//                    .message(sendMailRequest.getMessage())
//                    .build();