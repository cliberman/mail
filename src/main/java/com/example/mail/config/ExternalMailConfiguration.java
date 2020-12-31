package com.example.mail.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@RequiredArgsConstructor
@Configuration
@Data
@ConfigurationProperties("mail.external")

public class ExternalMailConfiguration {
    private String url;
    private String key;
    private String ip;
}
