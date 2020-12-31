package com.example.mail.service;

import com.example.mail.config.ExternalMailConfiguration;
import com.example.mail.config.FeatureSwitchConfiguration;
import com.example.mail.config.SendMailConfiguration;
import com.example.mail.controller.ExternalMailRequest;
import com.example.mail.controller.LoginInfo;
import com.example.mail.controller.SendMailRequest;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
@Service
@Data
@RequiredArgsConstructor
public class MailService {

    private final RestTemplate restTemplate;
    private ExternalMailConfiguration externalMailConfiguration;
    private FeatureSwitchConfiguration featureSwitchConfiguration;
    private SendMailConfiguration sendMailConfiguration;

    //login-done
    public static UUID getLoginInfo(LoginInfo loginInfo) {
                Map.Entry<LoginInfo, UUID> user = Users.userMap.entrySet().stream()
                .filter(e -> loginInfo.getUsername().equals(e.getKey().getUsername()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("No user"));
        if (loginInfo.getPassword().equals(user.getKey().getPassword())) {
            return (user.getValue());
        } else {
            throw new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        }
    }

    public Object sendEmail(SendMailRequest sendMailRequest) throws HttpClientErrorException {
        //if the server is down, return service unavailable status code
        if(sendMailConfiguration.isSendingUp()==false) {
            return HttpStatus.SERVICE_UNAVAILABLE;
        }
        else {
            LoginInfo from = validateFrom(sendMailRequest.getFrom());

            //go through usermap and find the to's full object
            try {
                LoginInfo to = Users.userMap.entrySet().stream()
                        .filter(e -> sendMailRequest.getTo().equals(e.getKey().getUsername()))
                        .findFirst()
                        .get()
                        .getKey();
                //.orElseThrow(() -> new Exception())
                //.getKey();

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
                return new ResponseEntity<>(HttpStatus.OK);

                //send the email to the external server because the recipient was not found locally:
            } catch (Exception e) {
                //System.out.println(e);
                //return HttpStatus.BAD_REQUEST;
                //RestTemplate restTemplate = new RestTemplate();
                ExternalMailRequest body = ExternalMailRequest.builder()
                        .from(sendMailRequest.getFrom())
                        .to(sendMailRequest.getTo())
                        .message(sendMailRequest.getMessage())
                        .build();
                String headerValue = new String(Base64.getEncoder().encode(externalMailConfiguration.getKey().getBytes()));
                HttpHeaders headers = new HttpHeaders();
                headers.add("api-key", headerValue);
                HttpEntity<ExternalMailRequest> httpEntity = new HttpEntity<>(body, headers);

                try {
                    ResponseEntity<Void> response = restTemplate.exchange("https://" + externalMailConfiguration.getUrl() + "/api/v1/email/receiveExternalMail",
                            HttpMethod.POST,
                            httpEntity,
                            Void.class);
                } catch (HttpStatusCodeException a) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                return new ResponseEntity<>(HttpStatus.OK);
            }
        }
    }

    /*  2. Modify our send endpoint:
        When a user sends an email, if the "to" address does not exists, today we reject it.
        We should modify this, so that if the to address does not exist, we should send that email to our external Email
        server. If that is successful, great, we are done. (There will be a bug here, that this email will not show up in the outbox).
        If we receive a 400 error from the external server, that means the user doesn't exist either on our server, or
        externally, we should return the same error we return today for recipient does not exist.
        Hint: You will need to use RestTemplate to send external API calls.
        The address of the external server must be in configuration. https://ti-timeserver.herokuapp.com/api/v1/email/receiveExternalMail
        There should be a feature switch to turn off sending externally. In this case, we would just say the user does not exist. */

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

        //check for the correct key
        String header = new String(Base64.getDecoder().decode(keyValue));
        if (header.equals(externalMailConfiguration.getKey())) {

            //if the server is down, return service unavailable status code
            if(featureSwitchConfiguration.isEmailUp()==false) {
                return HttpStatus.SERVICE_UNAVAILABLE;
            }

            //if the server is up, continue:
            else {
                //validate the from:
                LoginInfo from = validateFrom(externalMailRequest.getFrom());

                //add the email to the recipient's inbox:
                LoginInfo to = Users.userMap.entrySet().stream()
                        .filter(e -> externalMailRequest.getTo().equals(e.getKey().getUsername()))
                        .findFirst()
                        .get()
                        .getKey();
                if (to == null) {
                    return HttpStatus.BAD_REQUEST;
                } else {
                    to.getInbox().add(
                            SendMailRequest.builder()
                                    .from(externalMailRequest.getFrom())
                                    .to(externalMailRequest.getTo())
                                    .message(externalMailRequest.getMessage())
                                    .build());
                    return HttpStatus.OK;
                }
            }

        }
        //if key is wrong:
        else {
            return HttpStatus.UNAUTHORIZED;
        }
    }
}







