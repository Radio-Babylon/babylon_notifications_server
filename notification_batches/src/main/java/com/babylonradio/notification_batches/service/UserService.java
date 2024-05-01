package com.babylonradio.notification_batches.service;

import com.babylonradio.notification_service.publicnotification.enums.NotificationType;
import com.babylonradio.notification_service.publicnotification.model.FCMToken;
import com.babylonradio.notification_service.publicnotification.utils.TimeUtils;
import com.babylonradio.notification_service.publicnotification.utils.TopicUtils;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {
    public Integer hasPendingRequests(DocumentReference userReference) {
        Integer notificationCounter = 0;
        try {
            DocumentSnapshot userSnapshot = userReference.get().get();
            log.info("Looking for pending requests for user {} ...", userSnapshot.getData().get("Name").toString());
            if(userSnapshot.getData().get("connectionRequests") != null) {
                List<String> connectionsRequests = (List<String>) userSnapshot.getData().get("connectionRequests");
                notificationCounter += connectionsRequests.size();
            }
            if(userSnapshot.getData().get("groupChatInvitations") != null) {
                List<String> groupInvitations = (List<String>) userSnapshot.getData().get("groupChatInvitations");
                notificationCounter += groupInvitations.size();
            }
            if(userSnapshot.getData().get("chatRequests") != null) {
                List<String> chatRequests = (List<String>) userSnapshot.getData().get("chatRequests");
                notificationCounter += chatRequests.size();
            }
            return notificationCounter;
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            log.error("An error has occured during the research pending requests process : ");
            log.error(e.getMessage());
            return 0;
        }
    }

    public FCMToken fetchFCMToken(String userUID) {
        log.info("Fetching FCMToken...");
        Firestore firestoreClient = FirestoreClient.getFirestore(FirebaseApp.getInstance());
        FCMToken fcmToken = null;
        try {
            DocumentReference userReference = firestoreClient.collection("users").document(userUID).get().get().getReference();
            String fcmTokenId = userReference.get().get().getData().get("fcmToken").toString();
            Map<String, Object> fcmTokenData = firestoreClient.collection("fcmTokens").document(fcmTokenId).get().get().getData();
            return new FCMToken(fcmTokenData.get("value").toString(), TimeUtils.formatOffsetDateTime(fcmTokenData.get("expirationDate").toString()));
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            log.error("FCM Token was not fetched because: ");
            log.error(e.getMessage());
            return fcmToken;
        }
    }

    public List<String> removeFCMToken(String fcmTokenUID) {
        log.info("Removing FCMToken...");
        List<String> topics = new ArrayList<>();
        Firestore firestoreClient = FirestoreClient.getFirestore(FirebaseApp.getInstance());
        try {
            CollectionReference userCollection = firestoreClient.collection("users");
            List<QueryDocumentSnapshot> userDocuments = userCollection.whereEqualTo("fcmToken", fcmTokenUID).get().get().getDocuments();
            userDocuments.forEach(user -> {
                List<String> userTopics = new ArrayList<>();
                try {
                    List<QueryDocumentSnapshot> chatDocuments = firestoreClient.collection("chats").whereArrayContains("users", user.getId()).get().get().getDocuments();
                    List<QueryDocumentSnapshot> eventDocuments = firestoreClient.collection("events").whereArrayContains("attendies", user.getId()).get().get().getDocuments();
                    chatDocuments.forEach(chat -> userTopics.add(TopicUtils.mapTopic(chat.getData().get("chatName"))));
                    eventDocuments.forEach(event -> userTopics.add(TopicUtils.mapTopic(event.getData().get("title"))));
                } catch (InterruptedException | ExecutionException | NullPointerException e) {
                    log.error("Error occured during fetching user topics: ");
                    log.error(e.getMessage());
                    userTopics.clear();
                }
                Map<String, Object> userData = user.getData();
                userData.replace("fcmToken", "");
                user.getReference().set(userData);
                topics.addAll(userTopics);
            });
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            log.error("Error occured during fetching topics related to FCMToken: ");
            log.error(e.getMessage());
        }
        return topics;
    }
}
