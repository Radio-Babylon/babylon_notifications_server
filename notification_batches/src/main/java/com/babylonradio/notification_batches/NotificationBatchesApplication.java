package com.babylonradio.notification_batches;

import com.babylonradio.notification_service.publicnotification.configuration.NotificationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(NotificationProperties.class)
public class NotificationBatchesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationBatchesApplication.class, args);
    }

}
