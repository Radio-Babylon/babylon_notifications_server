package com.babylonradio.notification_service.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class Notification {
    private String id;
    private Type type;
    private Receiver receiver;
    private Sender sender;
    private Metadata metadata;
}
