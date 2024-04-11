package com.babylonradio.notification_service.model;

import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class Notification {
    private String id;
    private Type type;
    private Receiver receiver;
    private Metadata metadata;
}
