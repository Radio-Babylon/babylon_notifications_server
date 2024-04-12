package com.babylonradio.notification_service.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

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
    public String fetchFcmToken(String userUID) {
        log.info("Fetching FcmToken...");
        DocumentReference documentReference;
        try {
            documentReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("users").document(userUID).get().get().getReference();
            ArrayList<String> fcmTokens = (ArrayList<String>) documentReference.get().get().getData().get("tokens");
            return fcmTokens.get(0);
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            log.error("FCM Token was not fetched because: ");
            log.error(e.getMessage());
            return "";
        }
    }
}
