package com.babylonradio.notification_service.publicnotification.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "notification")
@Getter
@Setter
public class NotificationProperties {
    private String googleCredentials;
}
