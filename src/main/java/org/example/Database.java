package org.example;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.FieldPath;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.utilities.Pair;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.management.RuntimeErrorException;

public final class Database {

    private static final String COLLECTION_NAME = "users";
    private static final Firestore db;

    static {
        // Initialize Firestore using service account key
        try {
            FileInputStream serviceAccount = new FileInputStream("src/main/resources/com/example/asteroidsgame-e3baf-firebase-adminsdk-xgljb-f3dbba2a60.json");

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);

            db = FirestoreClient.getFirestore();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize Firestore", e);
        }
    }

    private Database() {
    }

    public static int getUserBestScore(String userName){
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userName);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = null;
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        if (document.exists()) {
            return document.getLong("bestScore").intValue();
        } else {
            return -1; // or any other default value
        }
    }

    public static void setUserBestScore(String userName, int score) {
        try {
            DocumentReference docRef = db.collection(COLLECTION_NAME).document(userName);
            Map<String, Object> data = new HashMap<>();
            data.put("bestScore", score);
            // Use SetOptions to merge with existing document if exists, or create new otherwise
            docRef.set(data, SetOptions.merge());

            // Retrieve and print the updated document
            ApiFuture<DocumentSnapshot> future = docRef.get();
            DocumentSnapshot document = future.get();
        } catch (Exception e) {
            System.err.println("Error updating best score for user " + userName + ": " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static boolean isUserRegistered(String userName){
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(userName);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        DocumentSnapshot document = null;
        try {
            document = future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return !document.exists();
    }

    public static List<LeaderboardEntry> getTop5ScoreList(){
        CollectionReference scoresCollection = db.collection(COLLECTION_NAME);
        Query query = scoresCollection.orderBy("bestScore", Query.Direction.DESCENDING).limit(5);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        List<LeaderboardEntry> leaderboard = new ArrayList<>();
        try {
            for (QueryDocumentSnapshot document : querySnapshot.get().getDocuments()) {
                leaderboard.add(new LeaderboardEntry(document.getId(), document.getLong("bestScore").intValue()));
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return leaderboard;
    }

}
