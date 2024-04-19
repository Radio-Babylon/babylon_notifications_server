package com.babylonradio.notification_service.service;

import com.babylonradio.notification_service.mapper.NotificationMapper;
import com.babylonradio.notification_service.publicnotification.utils.TimeUtils;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static com.babylonradio.notification_service.publicnotification.utils.TopicUtils.mapTopic;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;

    public void sendMessageToCloudMessaging(QueryDocumentSnapshot querySnapshot) {
        log.info("Start Message Generation Process ...");
        com.babylonradio.notification_service.publicnotification.model.Notification newNotification = notificationMapper.toNotification(querySnapshot);
        try {
            Message message = Message.builder()
//                .putAllData(metadataMapper.toMap(newNotification.getMetadata()))
                .setNotification(
                        Notification.builder()
                                .setTitle("Notification Debug Service")
                                .setImage(newNotification.getMetadata().getPicturesURLs().get(0))
                                .setBody("Here is your message delivery ! You receive a notification of type " + newNotification.getNotificationType() + " from " + newNotification.getSender().getUsername())
                                .build())
                .setToken(newNotification.getReceiver().getFcmToken().getValue())
                .build();
            FirebaseMessaging.getInstance().send(message);
            log.info("Message to FCM Registration Token sent successfully");
            popNotificationsData(newNotification.getId());
            log.info("Notification {} was removed from Firestore", newNotification.getId());
        } catch (FirebaseMessagingException | IllegalArgumentException e) {
            log.error("Message to FCM Registration Token was not sent");
        }
    }

    public void sendMessageToCloudMessaging(DocumentSnapshot docSnapshot, boolean isGroupChat) {
        log.info("Start Message Generation Process ...");
        try {
            com.babylonradio.notification_service.publicnotification.model.Notification newNotification = notificationMapper.toNotification(docSnapshot, isGroupChat);

            Message message = Message.builder()
//                .putAllData(metadataMapper.toMap(newNotification.getMetadata()))
                    .setNotification(
                            Notification.builder()
                                    .setTitle("Notification Debug Service")
                                    .setImage(newNotification.getMetadata().getPicturesURLs().get(0))
                                    .setBody("Here is your message delivery ! You receive a notification of type " + newNotification.getNotificationType() + ": " + newNotification.getMetadata().getMessages().get(0))
                                    .build())
                    .setTopic(mapTopic(newNotification.getMetadata().getTopic()))
                    .build();
            switch (newNotification.getNotificationType()) {
                default -> {
                    if(!isTheMessageNotified(docSnapshot.getId(), newNotification.getMetadata().getMessages().get(0), newNotification.getMetadata().getLastEventTimestamp())) {
                        FirebaseMessaging.getInstance().send(message);
                        log.info("Message to FCM Registration Token sent successfully for topic {}", newNotification.getMetadata().getTopic());
                        markMessageAsNotified(docSnapshot.getId(), newNotification.getMetadata().getMessages().get(0), newNotification.getMetadata().getLastEventTimestamp());
                    }
                    else {
                        log.warn("Message to FCM Registration Token was not sent successfully for topic {} because it was already emitted", newNotification.getMetadata().getTopic());
                    }
                }
            }
        } catch (FirebaseMessagingException | IllegalArgumentException e) {
            log.error("Message to FCM Registration Token was not sent");
        } catch (NullPointerException e) {
            log.error("Message to FCM Registration Token was not sent due to a lack of expected data: ");
            log.error(e.getMessage());
        }
    }

    private void popNotificationsData(String notificationId) {
        log.info("Removing notification {} ...", notificationId);
        FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("notifications").document(notificationId).delete();
    }

    private boolean isTheMessageNotified(String chatId, String messageContent, OffsetDateTime timestamp) {
        log.info("Looking for a \"notified\" mark ...");
        Boolean isNotified = false;
        try {
            DocumentReference documentReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("chats").document(chatId).collection("messages").orderBy("time", Query.Direction.DESCENDING).limit(1).get().get().getDocuments().get(0).getReference();
            DocumentSnapshot documentSnapshot = documentReference.get().get();
            if(documentSnapshot.get("message").toString().equals(messageContent) && TimeUtils.formatOffsetDateTime(documentSnapshot.get("time").toString()).equals(timestamp)) {
                isNotified = documentSnapshot.get("notified") != null ? (boolean) documentSnapshot.get("notified") : false;
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Message was not marked as notified because of: ");
            log.error(e.getMessage());
        }
        return isNotified;
    }

    private void markMessageAsNotified(String chatId, String messageContent, OffsetDateTime timestamp) {
        log.info("Marking message as notified ...");
        try {
            DocumentReference documentReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("chats").document(chatId).collection("messages").orderBy("time", Query.Direction.DESCENDING).limit(1).get().get().getDocuments().get(0).getReference();
            DocumentSnapshot documentSnapshot = documentReference.get().get();
            if(documentSnapshot.get("message").toString().equals(messageContent) && TimeUtils.formatOffsetDateTime(documentSnapshot.get("time").toString()).equals(timestamp)) {
                Map<String, Object> newFields = new HashMap<>();
                newFields.putAll(documentSnapshot.getData());
                newFields.put("notified", true);
                documentReference.set(newFields);
                log.info("Message was marked as notified");
            }
        } catch (InterruptedException | ExecutionException e) {
            log.error("Message was not marked as notified because of: ");
            log.error(e.getMessage());
        }
    }
}
