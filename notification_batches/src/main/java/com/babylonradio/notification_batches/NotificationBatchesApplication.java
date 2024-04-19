package com.babylonradio.notification_batches;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication
public class NotificationBatchesApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationBatchesApplication.class, args);
    }

}
