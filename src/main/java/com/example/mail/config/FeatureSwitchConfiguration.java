package com.example.mail.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("featureswitch")
public class FeatureSwitchConfiguration {
    private boolean printIp;
    private boolean emailUp;
}
