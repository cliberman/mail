package com.example.mail.controller;

import com.example.mail.config.ExternalMailConfiguration;
import com.example.mail.config.FeatureSwitchConfiguration;
import com.example.mail.service.Email;
import com.example.mail.service.MailService;
import io.swagger.annotations.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mail")
//@RequiredArgsConstructor

public class MailController {
    private final MailService mailService;
    private final ExternalMailConfiguration externalMailConfiguration;
    private final FeatureSwitchConfiguration featureSwitchConfiguration;
    private final SendMailRequest sendMailRequest;

    public MailController(MailService mailService, ExternalMailConfiguration externalMailConfiguration, FeatureSwitchConfiguration featureSwitchConfiguration, SendMailRequest sendMailRequest) {
        this.mailService = mailService;
        this.externalMailConfiguration = externalMailConfiguration;
        this.featureSwitchConfiguration = featureSwitchConfiguration;
        this.sendMailRequest = sendMailRequest;
    }

    @ApiOperation(notes = "test notes", value = "this is a value")
    @ApiResponses(value = {@ApiResponse(code=200, message="ok", response=String.class, examples = @Example(value = @ExampleProperty(mediaType = "UUID", value = "1234"))),
            @ApiResponse(code=401, message="unauthorized", response = String.class)})
    @PostMapping("/login")
    public UUID getLogin(@RequestBody LoginInfo loginInfo) {
//        if (featureSwitchConfiguration.isEmailUp()) {
//            return new ResponseEntity<>("Sorry, our server is down right now", HttpStatus.UNAUTHORIZED);
//        }
        return MailService.getLoginInfo(loginInfo);
    }

    @PostMapping("/send")
    public Object send(@RequestBody SendMailRequest sendMailRequest) {
        return mailService.sendEmail(sendMailRequest);
    }

    @PostMapping("/inbox")
    public ArrayList<Email> getInbox(@RequestBody String primaryKey)
    {
        return mailService.returnInbox(primaryKey);
    }

    @ResponseBody
    @PostMapping("/outbox")
    public ArrayList<Email> getOutbox(@RequestBody String primaryKey)
    {
        return mailService.returnOutbox(primaryKey);
    }

    @PostMapping("receiveExternalMail")
    public Object receiveExternalMail(@RequestBody ExternalMailRequest externalMailRequest,
                                      @RequestHeader("api-key") String key) {
        return mailService.receiveEmail(externalMailRequest, key);
    }
}


