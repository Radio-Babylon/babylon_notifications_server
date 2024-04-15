package com.babylonradio.notification_service.service;

import com.babylonradio.notification_service.mapper.NotificationMapper;
import com.babylonradio.notification_service.mapper.ReceiverMapper;
import com.babylonradio.notification_service.model.Receiver;
import com.babylonradio.notification_service.model.User;
import com.babylonradio.notification_service.utils.TopicUtils;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.babylonradio.notification_service.utils.TopicUtils.mapTopic;

@Service
@Slf4j
@AllArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final ReceiverMapper receiverMapper;

    public void subscribeToTopic(QueryDocumentSnapshot querySnapshot) {
        log.info("Start Subscription Process ...");
        List<String> userUID = (List<String>) querySnapshot.getData().get("users");
        List<Receiver> receivers = userUID.stream().map(receiverMapper::toReceiver).toList();
        List<String> newSubscribedTokens = receivers.stream().map(Receiver::getFcmToken).toList();
        String topicName = mapTopic(querySnapshot.getData().get("topic").toString());

        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(newSubscribedTokens, topicName);
            log.info("Numbers of tokens sucessfully subscribed: {}", response.getSuccessCount());
            log.info("Numbers of tokens unsucessfully subscribed: {}", response.getFailureCount());
            log.warn("Errors about subscription: {}", response.getErrors());
            if(response.getErrors().isEmpty()) {
                popSubscriptionData(String subscriptionId);
            }
        } catch (FirebaseMessagingException e) {
            log.error("Subscription failed because of: ");
            log.error(e.getMessage());
        }
    }

    public void unsubscribeFromTopic(QueryDocumentSnapshot querySnapshot) {
        log.info("Start Unsubscription Process ...");
        List<String> userUID = (List<String>) querySnapshot.getData().get("users");
        List<Receiver> receivers = userUID.stream().map(receiverMapper::toReceiver).toList();
        List<String> newSubscribedTokens = receivers.stream().map(Receiver::getFcmToken).toList();
        String topicName = mapTopic(querySnapshot.getData().get("topic").toString());

        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(newSubscribedTokens, topicName);
            log.info("Numbers of tokens sucessfully unsubscribed: {}", response.getSuccessCount());
            log.info("Numbers of tokens unsucessfully unsubscribed: {}", response.getFailureCount());
            if(response.getErrors().isEmpty()) {
                popSubscriptionData(String subscriptionId);
            }
        } catch (FirebaseMessagingException e) {
            log.error("Unsubscription failed because of: ");
            log.error(e.getMessage());
        }
    }

    public void sendMessageToCloudMessaging(QueryDocumentSnapshot querySnapshot) {
        log.info("Start Message Generation Process ...");
        com.babylonradio.notification_service.model.Notification newNotification = notificationMapper.toNotification(querySnapshot);
        try {
            Message message = Message.builder()
//                .putAllData(metadataMapper.toMap(newNotification.getMetadata()))
                .setNotification(
                        Notification.builder()
                                .setTitle("Notification Debug Service")
                                .setImage(newNotification.getMetadata().getPicturesURLs().get(0))
                                .setBody("Here is your message delivery ! You receive a notification of type " + newNotification.getNotificationType() + " from " + newNotification.getSender().getUsername())
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

    public void sendMessageToCloudMessaging(DocumentSnapshot docSnapshot, boolean isGroupChat) {
        log.info("Start Message Generation Process ...");
        com.babylonradio.notification_service.model.Notification newNotification = notificationMapper.toNotification(docSnapshot, isGroupChat);
        try {
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
            FirebaseMessaging.getInstance().send(message);
            log.info("Message to FCM Registration Token sent successfully");
        } catch (FirebaseMessagingException | IllegalArgumentException e) {
            log.error("Message to FCM Registration Token was not sent");
        }
    }

    private void popNotificationsData(String notificationId) {
        log.info("Removing notification {} ...", notificationId);
        FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("notifications").document(notificationId).delete();
    }

    private void popSubscriptionData(String subscriptionId) {
        log.info("Removing notification {} ...", subscriptionId);
        FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("notifications").document(subscriptionId).delete();
    }
}
