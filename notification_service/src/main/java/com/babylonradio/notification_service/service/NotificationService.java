package com.babylonradio.notification_service.service;

import com.babylonradio.notification_service.mapper.NotificationMapper;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
//    private final MetadataMapper metadataMapper;

    public void sendMessageToCloudMessaging(QueryDocumentSnapshot querySnapshot) {
        log.info("Start Message Generation Process ...");
        com.babylonradio.notification_service.model.Notification newNotification = notificationMapper.toNotification(querySnapshot);
        try {
            Message message = Message.builder()
//                .putAllData(metadataMapper.toMap(newNotification.getMetadata()))
                .setNotification(
                        Notification.builder()
                                .setTitle("Notification Debug Service")
                                .setBody("Here is your message delivery ! You receive a notification of type " + newNotification.getType() + " from " + newNotification.getSender().getUsername())
                                .build())
                .setToken(newNotification.getReceiver().getFcmToken())
                .build();
            FirebaseMessaging.getInstance().send(message);
            log.info("Message to FCM Registration Token sent successfully");
            popNotificationsData(newNotification.getId());
            log.info("Notification {} was removed from Firestore", newNotification.getId());
        } catch (FirebaseMessagingException | IllegalArgumentException e) {
            log.error("Message to FCM Registration Token was not sent");
        }
    }

    private void popNotificationsData(String messageId) {
        log.info("Removing notification {} ...", messageId);
        FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("notifications").document(messageId).delete();
    }
}
