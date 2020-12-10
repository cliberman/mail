package com.example.mail.controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginInfo {
    private String username;
    private String password;
    private ArrayList<SendMailRequest> inbox;
    private ArrayList<SendMailRequest> outbox;

    public LoginInfo(String username, String password) {
        this.username = username;
        this.password = password;
        this.inbox = new ArrayList<>();
        this.outbox = new ArrayList<>();
    }
}
