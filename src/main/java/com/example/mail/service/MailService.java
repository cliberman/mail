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


//    public boolean send() {
//    LoginInfo.EMAILS.add(Email.builder().build());
//    return true;
//    }

//    private final SendMailRequest sendMailRequest;

    public void sendEmail(SendMailRequest sendMailRequest) throws HttpClientErrorException {
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

        } catch (Exception e) {
//            ExternalMailRequest body = ExternalMailRequest.builder()
//                    .from(from)
//                    .to(sendMailRequest.getTo())
//                    .message(sendMailRequest.getMessage())
//                    .build();
            System.out.println(e);
//
//            String headerValue = new String(Base64.getEncoder().encode(externalMailConfiguration.getKey().getBytes()));
//            HttpHeaders headers = new HttpHeaders();
//            headers.add("api-key", headerValue);
//            //HttpEntity<ExternalMailRequest> httpEntity = new HttpEntity<>(body, headers);
//
//            ResponseEntity<Void> response = restTemplate.exchange("https://" + externalMailConfiguration.getUrl() + "/api/v1/email/receiveExternalMail", HttpMethod.POST, httpEntity, Void.class);
//
//            if(response.getStatusCode() != HttpStatus.OK) {
//                throw new HttpClientErrorException(HttpStatus.BAD_REQUEST);
//            }

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

    public ArrayList<SendMailRequest> returnInbox(UUID primaryKey) {
        LoginInfo user = Users.userMap.entrySet().stream()
                .filter(e -> primaryKey.equals(e.getValue()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("No user"))
                .getKey();
        ArrayList receivedMessages = user.getInbox();
        return receivedMessages;
    }

    public ArrayList<SendMailRequest> returnOutbox(UUID primaryKey) {
        LoginInfo user = Users.userMap.entrySet().stream()
                .filter(e -> primaryKey.equals(e.getValue()))
                .findFirst()
                .orElseThrow(() -> new NullPointerException("No user"))
                .getKey();
        ArrayList sentMessages = user.getOutbox();
        return sentMessages;
    }
}



//2. Send email -> POST /api/v1/email/send
//The post body should include the senders primary key (they got it from step 1), the username of the recipient,
// and the message, in JSON.
// If the username exists, then this should be saved to to the array of emails. (to, from, message).
// Remember that both to and from should be UUIDs, neither should be usernames.

/*3. retrieve email -> POST /api/v1/email/inbox
The post body should include the primary key of the user. Retrieve only that user's e-mails (the ones TO that user).
Json response body should be an array of objects, and each object should include a 'from' (username, not key),
and message.

4. retrieve outbox -> POST /api/v1/email/outbox
The post body should include the primary key of the user. Retrieve only that user's sent e-mails
(the ones FROM that user). Json response body should be and array of objects, and each object should include
a 'to' (username, not key), and message.
*/