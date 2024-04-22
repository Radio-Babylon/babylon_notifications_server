package com.babylonradio.notification_service.publicnotification.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Data
@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
public class Receiver extends User {
    private FCMToken fcmToken;
}
