package com.babylonradio.notification_service.service;

import com.babylonradio.notification_service.mapper.NotificationMapper;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class NotificationService {

    private static String collectionName = "notifications";
    private static String fieldName = "message";

    private final NotificationMapper notificationMapper;

    public static void sendMessageToFcmRegistrationToken(QueryDocumentSnapshot querySnapshot) throws Exception {
        String registrationToken = querySnapshot.getData().get("token").toString();
        Notification newNotification = notificationMapper.toNotification(querySnapshot);
        Message message = Message.builder()
                .putData("Message", querySnapshot.getData().get(fieldName).toString())
                .setNotification(
                        Notification.builder()
                                .setTitle("Notification Debug Service")
                                .setBody("Here is your message delivery : " + querySnapshot.getData().get(fieldName).toString())
                                .build())
                .setToken(registrationToken)
                .build();
        FirebaseMessaging.getInstance().send(message);
        removeNotificationsData(newNotification.getId());
    }

    private static void removeNotificationsData(String messageId) {
        FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection(collectionName).document(messageId).delete();
    }
}
