package com.babylonradio.notification_service.service;

import com.google.cloud.firestore.DocumentReference;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
@Slf4j
public class MetadataService {
    public List<String> fetchPictures(String userUID) {
        log.info("Fetching User Pictures ...");
        DocumentReference documentReference;
        try {
            documentReference = FirestoreClient.getFirestore(FirebaseApp.getInstance()).collection("users").document(userUID).get().get().getReference();
            return Arrays.asList(documentReference.get().get().getData().get("ImageUrl").toString());
        } catch (InterruptedException | ExecutionException | NullPointerException e) {
            log.error("Username was not fetched because: ");
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> fetchMessages(String userUID) {
        log.info("Fetching Messages ...");
        return null;
    }
}
