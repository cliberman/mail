package com.example.mail.controller;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel
public class LoginInfo {
    @ApiModelProperty
    private String username;
    @ApiModelProperty
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
