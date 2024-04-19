package com.babylonradio.notification_service.publicnotification.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Data
@Getter
@Setter
public class FCMToken {
    private final String value;
    private final OffsetDateTime expirationDate;
}
