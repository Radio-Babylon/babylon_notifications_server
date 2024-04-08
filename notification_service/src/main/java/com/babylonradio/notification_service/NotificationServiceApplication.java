package com.babylonradio.notification_service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.CollectionReference;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
public class NotificationServiceApplication {

    public static void main(String[] args) throws Exception {
        initFirebaseSDK();
        readWithListener();
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

    private static void initFirebaseSDK() throws Exception {
        FirebaseOptions options =
                FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(NotificationServiceApplication.class.getClassLoader().getResourceAsStream("service-account.json")))
                        .build();
        FirebaseApp.initializeApp(options);
    }

    public static void readWithListener() {
        CollectionReference documentReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("notifications");
        documentReference.addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (e != null) {
                System.out.println(e.getMessage());
                return;
            }
            if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                queryDocumentSnapshots.getDocuments().forEach(doc -> {
                    try {
                        sendMessageToFcmRegistrationToken(doc.getData());
                        System.out.println("Message to FCM Registration Token sent successfully !!");
                        removeNotificationsData(doc.getId());
                    } catch (Exception ex) {
                        System.out.println("Message to FCM Registration Token was not sent !!");
                    }
                });
            }
        });
    }

    private static void sendMessageToFcmRegistrationToken(Map<String, Object> messageToSend) throws Exception {
        String registrationToken = "dMxgvNM4QJyLsIJApxnGBh:APA91bHF-Yf_LqhfEvkK_DV7G3NMDylajL4qZylXQAza5RJWbR0848ML88IDIWGEuKOFc1OFfbNd0Q2XJJo3-_pD-Ik-v3jOeB_xxtUs6WW1THsGvu36IVI21_xLSiOlXRt2OPuTOTUt";
        Message message = Message.builder()
                        .putData("Message", messageToSend.get("message").toString())
                        .setNotification(
                                Notification.builder()
                                        .setTitle("Notification Service")
                                        .setBody("Here is your message delivery : " + messageToSend.get("message").toString())
                                        .build())
                        .setToken(registrationToken)
                        .build();
        FirebaseMessaging.getInstance().send(message);
    }

    private static void removeNotificationsData(String messageId) {
        FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("notifications").document(messageId).delete();
    }
}
