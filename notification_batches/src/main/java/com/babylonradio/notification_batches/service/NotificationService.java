package com.babylonradio.notification_batches.service;

import com.babylonradio.notification_service.publicnotification.enums.NotificationType;
import com.babylonradio.notification_service.publicnotification.model.FCMToken;
import com.babylonradio.notification_service.publicnotification.model.Metadata;
import com.babylonradio.notification_service.publicnotification.model.Notification;
import com.babylonradio.notification_service.publicnotification.model.Receiver;
import com.babylonradio.notification_service.publicnotification.utils.TopicUtils;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationService {
    public Notification buildPendingRequestNotification(Integer notificationNumber, FCMToken userToken) {
        Receiver receiver = new Receiver();
        receiver.setFcmToken(userToken);
        Metadata metadata = Metadata.builder()
                .messages(List.of( "You have " + notificationNumber + " pending requests ! Have a look to it !"))
                .build();
        return Notification.builder()
                .receiver(receiver)
                .metadata(metadata)
                .notificationType(NotificationType.PENDING_REQUESTS_REMINDER)
                .build();
    }

    public Notification buildEventNotification(String eventName) {
        Metadata metadata = Metadata.builder()
                .topic(TopicUtils.mapTopic(eventName))
                .messages(List.of( "Do not forget to join us at " + eventName + " ! It is tomorrow !"))
                .build();
        return Notification.builder()
                .metadata(metadata)
                .notificationType(NotificationType.EVENT_REMINDER)
                .build();
    }

    public void sendMessage(Notification notification) {
        try {
            Message message = null;
            if(notification.getReceiver() != null) {
                message = Message.builder()
                        .setNotification(
                                com.google.firebase.messaging.Notification.builder()
                                        .setTitle("Notification Debug Service")
                                        .setBody("Here is your message delivery ! You receive a notification of type " + notification.getNotificationType() + ": " + notification.getMetadata().getMessages().get(0))
                                        .build())
                        .setToken(notification.getReceiver().getFcmToken().getValue())
                        .build();
                FirebaseMessaging.getInstance().send(message);
                log.info("Message to FCM Registration Token was sent");
            }
            else {
                message = Message.builder()
                        .setNotification(
                                com.google.firebase.messaging.Notification.builder()
                                        .setTitle("Notification Debug Service")
                                        .setBody("Here is your message delivery ! You receive a notification of type " + notification.getNotificationType() + ": " + notification.getMetadata().getMessages().get(0))
                                        .build())
                        .setTopic(notification.getMetadata().getTopic())
                        .build();
                FirebaseMessaging.getInstance().send(message);
                log.info("Message to FCM Registration Token was sent for topic {}", notification.getMetadata().getTopic());
            }
        } catch (FirebaseMessagingException | IllegalArgumentException e) {
            log.error("Message to FCM Registration Token was not sent");
        }
    }
}
