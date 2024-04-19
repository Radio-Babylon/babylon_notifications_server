package com.babylonradio.notification_batches.service;

import com.babylonradio.notification_service.publicnotification.model.FCMToken;
import com.babylonradio.notification_service.publicnotification.model.Receiver;
import com.babylonradio.notification_service.publicnotification.model.Subscription;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class SubscriptionService {
    public void unsubscribeFromTopic(List<String> topics, String fcmTokenToPurge) {
        log.info("Start Unsubscription Process ...");
        for(String topic : topics) {
            try {
                TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(List.of(fcmTokenToPurge), topic);
                if(response.getFailureCount() > 0) {
                    log.warn("Token unsucessfully unsubscribed");
                    log.warn("Errors about unsubscription: {}", response.getErrors());
                }
                else {
                    log.info("Token sucessfully unsubscribed");
                }
            } catch (FirebaseMessagingException | IllegalArgumentException e) {
                log.error("Unsubscription failed because of: ");
                log.error(e.getMessage());
            }
        }
    }
}
