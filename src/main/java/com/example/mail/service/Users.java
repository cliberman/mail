package com.example.mail.service;

import com.example.mail.controller.LoginInfo;
import com.example.mail.controller.SendMailRequest;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class Users {
    public static HashMap<LoginInfo, UUID> userMap = mapUsers();

    public static HashMap<LoginInfo, UUID> mapUsers() {
        HashMap<LoginInfo, UUID> tempMap = new HashMap<>();
        tempMap.put(new LoginInfo("Chana", "password"), UUID.randomUUID());
        tempMap.put(new LoginInfo("nameless", "mypassword"), UUID.randomUUID());
        tempMap.put(new LoginInfo("joeordonald", "2020"), UUID.randomUUID());
        return tempMap;
    }
}


