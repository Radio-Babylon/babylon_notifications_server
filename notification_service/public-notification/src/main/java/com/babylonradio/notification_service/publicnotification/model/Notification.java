package com.babylonradio.notification_service.publicnotification.model;

import com.babylonradio.notification_service.publicnotification.enums.NotificationType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@Builder
public class Notification {
    private String id;
    private NotificationType notificationType;
    private Receiver receiver;
    private Sender sender;
    private Metadata metadata;
}
