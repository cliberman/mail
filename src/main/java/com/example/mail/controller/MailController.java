package com.example.mail.controller;

import com.example.mail.service.MailService;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/mail")
//@RequiredArgsConstructor

public class MailController {
    private final MailService mailService;
    private final ExternalMailConfiguration externalMailConfiguration;
    //private final FeatureSwitchConfiguration featureSwitchConfiguration;
    private final SendMailRequest sendMailRequest;

    public MailController(MailService mailService, ExternalMailConfiguration externalMailConfiguration, SendMailRequest sendMailRequest) {
        this.mailService = mailService;
        this.externalMailConfiguration = externalMailConfiguration;
        //this.featureSwitchConfiguration = featureSwitchConfiguration;
        this.sendMailRequest = sendMailRequest;
    }

    @PostMapping("/login")
    public UUID getLogin(@RequestBody LoginInfo loginInfo) {
        return MailService.getLoginInfo(loginInfo);
    }

    @PostMapping("receiveExternalMail")
    public Object receiveExternalMail(@RequestBody ExternalMailRequest externalMailRequest,
                                      @RequestHeader("api-key") String key) {
        return null;
    }

    @PostMapping("/send")
    public Object send(@RequestBody SendMailRequest sendMailRequest) {
        mailService.sendEmail(sendMailRequest);
        return null;
    }

    //retrieve email -> POST /api/v1/email/inbox
    @PostMapping("/inbox")
    public ArrayList<SendMailRequest> getInbox(@RequestBody UUID primaryKey)
    {
        return mailService.returnInbox(primaryKey);
    }

    @ResponseBody
    @PostMapping("/outbox")
    public ArrayList<SendMailRequest> getOutbox(@RequestBody UUID primaryKey)
    {
        return mailService.returnOutbox(primaryKey);
    }
}


