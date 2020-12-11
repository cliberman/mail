package com.example.mail.controller;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ExternalMailRequest {
    private UUID from;
    private String to;
    private String message;
}
