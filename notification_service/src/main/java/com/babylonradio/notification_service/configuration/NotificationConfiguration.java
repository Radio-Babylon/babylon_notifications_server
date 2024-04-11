package com.babylonradio.notification_service.configuration;


import com.babylonradio.notification_service.NotificationServiceApplication;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

import static com.babylonradio.notification_service.service.NotificationService.sendMessageToFcmRegistrationToken;

@Configuration
@Slf4j
public class NotificationConfiguration {
    @Bean
    public FirebaseApp initFirebaseApp() throws IOException {
        FirebaseOptions options =
                FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(NotificationServiceApplication.class.getClassLoader().getResourceAsStream("service-account.json")))
                        .build();
        return FirebaseApp.initializeApp(options);
    }

    @Bean
    public CollectionReference readNotification(FirebaseApp firebaseApp) {
        CollectionReference collectionReference = FirestoreClient.getFirestore(firebaseApp.getInstance()).collection("notifications");
        collectionReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                log.info(e.getMessage());
                return;
            }
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                queryDocumentSnapshots.getDocuments().forEach(doc -> {
                    try {
                        sendMessageToFcmRegistrationToken(doc);
                        log.info("Message to FCM Registration Token sent successfully !!");
                    } catch (Exception ex) {
                        log.error("Message to FCM Registration Token was not sent !!");
                    }
                });
            }
        });
        return collectionReference;
    }
}
