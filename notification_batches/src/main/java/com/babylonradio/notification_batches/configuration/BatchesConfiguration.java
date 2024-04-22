package com.babylonradio.notification_batches.configuration;

import com.babylonradio.notification_batches.NotificationBatchesApplication;
import com.babylonradio.notification_batches.service.NotificationService;
import com.babylonradio.notification_batches.service.SubscriptionService;
import com.babylonradio.notification_batches.service.UserService;
import com.babylonradio.notification_service.publicnotification.enums.NotificationType;
import com.babylonradio.notification_service.publicnotification.model.FCMToken;
import com.babylonradio.notification_service.publicnotification.model.Notification;
import com.babylonradio.notification_service.publicnotification.utils.TimeUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.w3c.dom.ranges.Range;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Configuration
@Slf4j
@EnableScheduling
@AllArgsConstructor
public class BatchesConfiguration {

    private final UserService userService;
    private final NotificationService notificationService;

    private final SubscriptionService subscriptionService;
    @Bean
    public FirebaseApp initFirebaseApp() throws IOException {
        FirebaseOptions options =
                FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(NotificationBatchesApplication.class.getClassLoader().getResourceAsStream("service-account.json")))
                        .build();
        return FirebaseApp.initializeApp(options);
    }

    @Scheduled(timeUnit = TimeUnit.SECONDS, initialDelay = 1, fixedRate = 86400)
    public void purgeExpiredTokens() {
        log.info("Purge Expired Tokens Job is starting ...");
        purgeTokens();
        log.info("Purge Expired Tokens Job is finished");
    }

    @Scheduled(timeUnit = TimeUnit.SECONDS, initialDelay = 100, fixedRate = 86400)
    public void requestPendingReminders() {
        log.info("Request Pending Reminder Job is starting ...");
        notifyPendingRequestsToUsers();
        log.info("Request Pending Reminder Job is finished");
    }

    @Scheduled(timeUnit = TimeUnit.SECONDS, initialDelay = 200, fixedRate = 86400)
    public void eventReminders() {
        log.info("Event Reminder Job is starting ...");
        notifyEventsToUsers();
        log.info("Event Reminder Job is finished");
    }

    private void purgeTokens() {
        log.info("Looking for expired FCMTokens ...");
        try {
            CollectionReference tokenCollection = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("fcmTokens");
            List<QueryDocumentSnapshot> documents = tokenCollection.get().get().getDocuments();
            documents.forEach(fcmToken -> {
                FCMToken fcmTokenFetched = new FCMToken(fcmToken.getData().get("value").toString(), TimeUtils.formatOffsetDateTime(fcmToken.getData().get("expirationDate").toString()));
                if(fcmTokenFetched.getExpirationDate().isBefore(OffsetDateTime.now().minusDays(1))) {
                    Message message = Message.builder()
                            .setToken(fcmTokenFetched.getValue())
                            .build();
                    try {
                        FirebaseMessaging.getInstance().send(message);
                        Map<String, Object> refreshedFcmToken = Map.of("value", fcmTokenFetched.getValue(), "expirationDate", OffsetDateTime.now());
                        tokenCollection.document(fcmToken.getId()).set(refreshedFcmToken);
                        log.info("FCMToken {} has been refreshed", fcmToken.getId());
                    } catch (FirebaseMessagingException e) {
                        List<String> topics = userService.removeFCMToken(fcmToken.getId());
                        subscriptionService.unsubscribeFromTopic(topics, fcmTokenFetched.getValue());
                        tokenCollection.document(fcmToken.getId()).delete();
                        log.info("FCMToken {} has been purged", fcmToken.getId());
                    }
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            log.error("Job ceased to run because of: ");
            log.error(e.getMessage());
        } catch (NullPointerException e) {
            log.error("Job ceased to run because of a lack of data: ");
            log.error(e.getMessage());
        }
    }

    private void notifyPendingRequestsToUsers() {
        CollectionReference userCollection = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("users");
        userCollection.listDocuments().forEach(user -> {
            Integer notificationCounter = userService.hasPendingRequests(user);
            Notification notification = notificationService.buildPendingRequestNotification(notificationCounter, userService.fetchFCMToken(user.getId()));
            if(notification.getReceiver().getFcmToken() != null) {
                notificationService.sendMessage(notification);
            }
        });
    }

    private void notifyEventsToUsers() {
        CollectionReference userCollection = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("events");
        userCollection.listDocuments().forEach(event -> {
            try {
                Map<String, Object> eventData = event.get().get().getData();
                OffsetDateTime eventTime = TimeUtils.formatOffsetDateTime(eventData.get("date").toString());
                if(eventTime.minusDays(OffsetDateTime.now().getDayOfYear()).getDayOfYear() == 1) {
                    Notification notification = notificationService.buildEventNotification(eventData.get("title").toString());
                    notificationService.sendMessage(notification);
                }
            } catch (InterruptedException | ExecutionException e) {
                log.error("Job ceased to run because of: ");
                log.error(e.getMessage());
            } catch (NullPointerException e) {
                log.error("Job ceased to run because of a lack of data: ");
                log.error(e.getMessage());
            }
        });
    }
}
