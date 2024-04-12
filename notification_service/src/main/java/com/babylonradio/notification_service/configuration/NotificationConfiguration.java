package com.babylonradio.notification_service.configuration;


import com.babylonradio.notification_service.NotificationServiceApplication;
import com.babylonradio.notification_service.service.NotificationService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
@AllArgsConstructor
public class NotificationConfiguration {

    private final NotificationService notificationService;
    @Bean
    public FirebaseApp initFirebaseApp() throws IOException {
        FirebaseOptions options =
                FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(NotificationServiceApplication.class.getClassLoader().getResourceAsStream("service-account.json")))
                        .build();
        return FirebaseApp.initializeApp(options);
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
