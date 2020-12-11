package com.example.mail.service;

import com.example.mail.controller.SendMailRequest;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;

@Data
@Builder
public class Email {
    private final String to;
    private final String message;

    public Email(String to, String message) {
        this.to = to;
        this.message = message;
    }
}
