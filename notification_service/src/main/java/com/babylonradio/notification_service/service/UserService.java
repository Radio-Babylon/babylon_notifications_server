package com.babylonradio.notification_service.service;

import com.babylonradio.notification_service.publicnotification.model.FCMToken;
import com.babylonradio.notification_service.publicnotification.utils.TimeUtils;
import com.babylonradio.notification_service.publicnotification.utils.TopicUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final SubscriptionService subscriptionService;

    public String fetchUsername(String userUID) {
        log.info("Fetching Username ...");
        DocumentReference documentReference;
        try {
            documentReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("users").document(userUID).get().get().getReference();
            return documentReference.get().get().getData().get("Name").toString();
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            log.error("Username was not fetched because: ");
            log.error(e.getMessage());
            return "";
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
        } catch (InterruptedException | ExecutionException | NullPointerException | IllegalArgumentException e) {
            log.error("FCM Token was not fetched because: ");
            log.error(e.getMessage());
            return fcmToken;
        }
    }

    public List<String> fetchTopics(String userUID) {
        log.info("Fetching topics ...");
        Firestore firestoreClient = FirestoreClient.getFirestore(FirebaseApp.getInstance());
        List<String> topics = new ArrayList<>();
        try {
            List<QueryDocumentSnapshot> chatDocuments = firestoreClient.collection("chats").whereArrayContains("users", userUID).get().get().getDocuments();
            List<QueryDocumentSnapshot> eventDocuments = firestoreClient.collection("events").whereArrayContains("attendies", userUID).get().get().getDocuments();
            chatDocuments.forEach(chat -> topics.add(TopicUtils.mapTopic(chat.getData().get("chatName"))));
            eventDocuments.forEach(event -> topics.add(TopicUtils.mapTopic(event.getData().get("title"))));
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            log.error("Error occured during fetching user topics: ");
            log.error(e.getMessage());
            topics.clear();
        }
        return topics;
    }

    public void registerToken(FCMToken fcmToken, String tokenUID, String userUID) {
        log.info("Registering FCMToken ...");
        CollectionReference tokenCollection = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("fcmTokens");
        CollectionReference userCollection = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("users");
        DocumentReference tokenDocument = null;
        try {
            List<QueryDocumentSnapshot> sameFCMToken = tokenCollection.select("value").whereEqualTo("value", fcmToken.getValue()).get().get().getDocuments();
            if(sameFCMToken.isEmpty()) {
                Map<String, Object> newFcmToken = Map.of("value", fcmToken.getValue(), "expirationDate", Timestamp.parseTimestamp(fcmToken.getExpirationDate().toString()));
                tokenDocument = tokenCollection.add(newFcmToken).get();
            }
            else {
                Map<String, Object> refreshedFcmToken = tokenCollection.document(sameFCMToken.get(0).getId()).get().get().getData();
                refreshedFcmToken.replace("expirationDate", Timestamp.parseTimestamp(fcmToken.getExpirationDate().toString()));
                tokenCollection.document(sameFCMToken.get(0).getId()).set(refreshedFcmToken);
                tokenDocument = tokenCollection.document(sameFCMToken.get(0).getId());
            }
            Map<String, Object> userData = userCollection.document(userUID).get().get().getData();
            userData.put("fcmToken", tokenDocument.getId());
            userCollection.document(userUID).set(userData);
            log.info("FCMToken for user {} was reset", userUID);
            FCMToken tokenToUnregister = fetchFCMToken(userUID);
            List<String> topics = fetchTopics(userUID);
            if(tokenToUnregister != null) {
                subscriptionService.unsubscribeFromTopic(topics, tokenToUnregister.getValue());
            }
            subscriptionService.subscribeFromTopic(topics, fcmToken.getValue());
            popPushedTokenData(tokenUID);
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            log.error("FCM Token was not registered because: ");
            log.error(e.getMessage());
        }
    }

    private void popPushedTokenData(String pushedTokenId) {
        log.info("Removing Pushed Token {} ...", pushedTokenId);
        FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("pushedTokens").document(pushedTokenId).delete();
    }
}
