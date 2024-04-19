package com.babylonradio.notification_service.publicnotification.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Receiver extends User {
    private FCMToken fcmToken;
}
