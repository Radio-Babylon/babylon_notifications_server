package com.babylonradio.notification_service.configuration;


import com.babylonradio.notification_service.NotificationServiceApplication;
import com.babylonradio.notification_service.publicnotification.configuration.NotificationProperties;
import com.babylonradio.notification_service.publicnotification.enums.SubscriptionType;
import com.babylonradio.notification_service.publicnotification.model.FCMToken;
import com.babylonradio.notification_service.publicnotification.utils.TimeUtils;
import com.babylonradio.notification_service.service.NotificationService;
import com.babylonradio.notification_service.service.SubscriptionService;
import com.babylonradio.notification_service.service.UserService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;

@Configuration
@Slf4j
@AllArgsConstructor
public class NotificationConfiguration {
    private final NotificationService notificationService;
    private final SubscriptionService subscriptionService;
    private final UserService userService;
    private final NotificationProperties notificationProperties;

    @Bean
    public FirebaseApp initFirebaseApp() throws IOException {
        FirebaseOptions options =
                FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(NotificationServiceApplication.class.getClassLoader().getResourceAsStream(notificationProperties.getGoogleCredentials())))
                        .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public CollectionReference readTokens() {
        CollectionReference collectionReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("pushedTokens");
        collectionReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                log.error("Snapshot Listener was not initialized because of:");
                log.error(e.getMessage());
                return;
            }
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                queryDocumentSnapshots.getDocuments().forEach(doc -> {
                    OffsetDateTime creationDate = TimeUtils.formatOffsetDateTime(doc.getData().get("creationDate").toString());
                    OffsetDateTime expirationDate = creationDate.plusDays(270);
                    userService.registerToken(new FCMToken(doc.getData().get("value").toString(), expirationDate), doc.getId(), doc.getData().get("user").toString());
                });
            }
        });
        return collectionReference;
    }

    @Bean
    public CollectionReference readSubscriptions() {
        CollectionReference collectionReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("subscriptions");
        collectionReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                log.error("Snapshot Listener was not initialized because of:");
                log.error(e.getMessage());
                return;
            }
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                queryDocumentSnapshots.getDocuments().forEach(doc -> {
                    SubscriptionType subscriptionType = SubscriptionType.valueOf(doc.getData().get("type").toString());
                    switch (subscriptionType) {
                        case SUBSCRIPTION -> subscriptionService.subscribeToTopic(doc);
                        case UNSUBSCRIPTION -> subscriptionService.unsubscribeFromTopic(doc);
                    }
                });
            }
        });
        return collectionReference;
    }

    @Bean
    public CollectionReference sendNotificationsForTopic() {
        CollectionReference collectionReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("chats");
        collectionReference.listDocuments().forEach(doc -> doc.addSnapshotListener((documentSnapshot, e) -> {
                if (e != null) {
                    log.error("Snapshot Listener was not initialized because of:");
                    log.error(e.getMessage());
                    return;
                }
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    notificationService.sendMessageToCloudMessaging(documentSnapshot, documentSnapshot.get("admin") != null);
                }
            })
        );
        return collectionReference;
    }

    @Bean
    public CollectionReference readNotifications() {
        CollectionReference collectionReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("notifications");
        collectionReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                log.error("Snapshot Listener was not initialized because of:");
                log.error(e.getMessage());
                return;
            }
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                queryDocumentSnapshots.getDocuments().forEach(doc -> notificationService.sendMessageToCloudMessaging(doc));
            }
        });
        return collectionReference;
    }
}
