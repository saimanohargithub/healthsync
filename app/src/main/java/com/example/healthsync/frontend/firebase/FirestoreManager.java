package com.example.healthsync.frontend.firebase;

import android.util.Log;

import com.example.healthsync.frontend.models.Challenge;
import com.example.healthsync.frontend.models.UserProfile;
import com.example.healthsync.frontend.models.UserRank;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    public interface ChallengeListener {
        void onChallengesLoaded(List<Challenge> challenges);
    }

    public interface LeaderboardListener {
        void onLeaderboardLoaded(List<UserRank> ranks);
    }

    public interface BadgeListener {
        void onBadgesLoaded(List<String> badges);
    }

    // Save user profile
    public static void saveUser(
            String uid,
            UserProfile profile
    ) {

        db.collection("users")
                .document(uid)
                .set(profile)
                .addOnSuccessListener(aVoid ->
                        System.out.println("User saved successfully"))
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // Get user profile
    public static void getUser(
            String uid,
            OnSuccessListener<DocumentSnapshot> listener
    ) {

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    // Challenge Methods

    public static void joinChallenge(String uid, Challenge challenge) {
        challenge.setJoined(true);
        db.collection("user_challenges")
                .document(uid)
                .collection("active")
                .document(challenge.getId())
                .set(challenge);
    }

    public static void updateChallengeProgress(String uid, String challengeId, int progress, boolean completed) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentProgress", progress);
        updates.put("completed", completed);

        db.collection("user_challenges")
                .document(uid)
                .collection("active")
                .document(challengeId)
                .update(updates);
    }

    public static void loadUserChallenges(String uid, ChallengeListener listener) {
        db.collection("user_challenges")
                .document(uid)
                .collection("active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Challenge> list = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Challenge c = doc.toObject(Challenge.class);
                        if (c != null) list.add(c);
                    }
                    listener.onChallengesLoaded(list);
                });
    }

    public static void awardChallengeReward(String uid, int points) {
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Long current = doc.getLong("points");
                if (current == null) current = 0L;
                db.collection("users").document(uid).update("points", current + points);
            }
        });
    }

    public static void loadLeaderboard(LeaderboardListener listener) {
        db.collection("users")
                .orderBy("points", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<UserRank> ranks = new ArrayList<>();
                    int rank = 1;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("name");
                        Long pts = doc.getLong("points");
                        if (name != null) {
                            ranks.add(new UserRank(rank++, name, pts != null ? pts.intValue() : 0, doc.getId()));
                        }
                    }
                    listener.onLeaderboardLoaded(ranks);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreManager", "Leaderboard load failed due to strict security rules.", e);
                    listener.onLeaderboardLoaded(new ArrayList<>());
                });
    }

    public static void unlockBadge(String uid, String badgeName) {
        Map<String, Object> badge = new HashMap<>();
        badge.put("name", badgeName);
        badge.put("timestamp", System.currentTimeMillis());

        db.collection("users").document(uid).collection("badges").document(badgeName).set(badge);
    }

    public static void loadUserBadges(String uid, BadgeListener listener) {
        db.collection("users").document(uid).collection("badges").get().addOnSuccessListener(query -> {
            List<String> list = new ArrayList<>();
            for (DocumentSnapshot doc : query) {
                list.add(doc.getString("name"));
            }
            listener.onBadgesLoaded(list);
        });
    }

    public static void saveMealPlan(com.example.healthsync.frontend.data.local.MealPlanEntity entity, com.example.healthsync.frontend.data.model.MealPlanModel model) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .collection("meal_plans")
                .document(entity.date)
                .set(model)
                .addOnSuccessListener(aVoid -> Log.d("FirestoreManager", "Meal plan saved to cloud"))
                .addOnFailureListener(e -> Log.e("FirestoreManager", "Cloud save failed", e));
    }

    public static void saveSleepEntry(com.example.healthsync.frontend.data.local.SleepEntryEntity entry) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .collection("sleep_logs")
                .document(entry.getDate())
                .set(entry)
                .addOnSuccessListener(aVoid -> Log.d("SLEEP_SAVE", "Saved to Firestore: " + entry.getDate()))
                .addOnFailureListener(e -> Log.e("SLEEP_SAVE", "Firestore error: " + e.getMessage()));
    }

    public static void saveMoodLog(com.example.healthsync.frontend.data.local.MoodLog log) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());
        String date = sdf.format(new java.util.Date(log.getTimestamp()));

        db.collection("users")
                .document(uid)
                .collection("mood_logs")
                .document(date)
                .set(log);
    }

    public static void syncChallengeProgress(String uid, String type, int progress) {
        db.collection("user_challenges")
                .document(uid)
                .collection("active")
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        Challenge c = doc.toObject(Challenge.class);
                        if (c != null && !c.isCompleted()) {
                            boolean completed = progress >= c.getGoal();
                            updateChallengeProgress(uid, c.getId(), progress, completed);
                        }
                    }
                });
    }
}


