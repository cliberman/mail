package com.example.mail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
@ConfigurationProperties("mail.external")

public class ExternalMailConfiguration {
    private String url;
    private String key;
    private String ip;
}
