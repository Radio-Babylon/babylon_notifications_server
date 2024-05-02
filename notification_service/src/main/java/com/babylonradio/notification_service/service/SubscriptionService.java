package com.babylonradio.notification_service.service;

import com.babylonradio.notification_service.mapper.SubscriptionMapper;
import com.babylonradio.notification_service.publicnotification.model.FCMToken;
import com.babylonradio.notification_service.publicnotification.model.Receiver;
import com.babylonradio.notification_service.publicnotification.model.Subscription;
import com.babylonradio.notification_service.publicnotification.utils.TopicUtils;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.TopicManagementResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@AllArgsConstructor
public class SubscriptionService {
    private final SubscriptionMapper subscriptionMapper;

    public void subscribeToTopic(QueryDocumentSnapshot querySnapshot) {
        log.info("Start Subscription Process ...");
        Subscription subscription = subscriptionMapper.toSubscription(querySnapshot);
        List<String> newSubscribedTokens = subscription.getUsers().stream().map(Receiver::getFcmToken).map(FCMToken::getValue).toList();
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(newSubscribedTokens, subscription.getTopic());
            log.info("Numbers of tokens sucessfully subscribed: {}", response.getSuccessCount());
            if(response.getFailureCount() > 0) {
                log.warn("Numbers of tokens unsucessfully subscribed: {}", response.getFailureCount());
                log.warn("Errors about subscription: {}", response.getErrors());
            }
            if(response.getErrors().isEmpty()) {
                popSubscriptionData(subscription.getId());
            }
        } catch (FirebaseMessagingException e) {
            log.error("Subscription failed because of: ");
            log.error(e.getMessage());
        }
    }

    public void unsubscribeFromTopic(QueryDocumentSnapshot querySnapshot) {
        log.info("Start Unsubscription Process ...");
        Subscription unsubscription = subscriptionMapper.toSubscription(querySnapshot);
        List<String> newUnsubscribedTokens = unsubscription.getUsers().stream().map(Receiver::getFcmToken).map(FCMToken::getValue).toList();
        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(newUnsubscribedTokens, unsubscription.getTopic());
            log.info("Numbers of tokens sucessfully unsubscribed: {}", response.getSuccessCount());
            if(response.getFailureCount() > 0) {
                log.warn("Numbers of tokens unsucessfully unsubscribed: {}", response.getFailureCount());
                log.warn("Errors about unsubscription: {}", response.getErrors());
            }
            if(response.getErrors().isEmpty()) {
                popSubscriptionData(unsubscription.getId());
            }
        } catch (FirebaseMessagingException e) {
            log.error("Unsubscription failed because of: ");
            log.error(e.getMessage());
        }
    }

    public void subscribeFromTopic(List<String> topics, String fcmTokenToRegister) {
        log.info("Start Subscription Process ...");
        for(String topic : topics) {
            try {
                TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(List.of(fcmTokenToRegister), topic);
                if(response.getFailureCount() > 0) {
                    log.warn("Token unsucessfully subscribed");
                    log.warn("Errors about unsubscription: {}", response.getErrors());
                }
                else {
                    log.info("Token sucessfully subscribed");
                }
            } catch (FirebaseMessagingException | IllegalArgumentException e) {
                log.error("Subscription failed because of: ");
                log.error(e.getMessage());
            }
        }
    }

    public void unsubscribeFromTopic(List<String> topics, String fcmTokenToUnregister) {
        log.info("Start Unsubscription Process ...");
        for(String topic : topics) {
            try {
                TopicManagementResponse response = FirebaseMessaging.getInstance().unsubscribeFromTopic(List.of(fcmTokenToUnregister), topic);
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

    private void popSubscriptionData(String subscriptionId) {
        log.info("Removing subscription {} ...", subscriptionId);
        FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("subscriptions").document(subscriptionId).delete();
    }

}
