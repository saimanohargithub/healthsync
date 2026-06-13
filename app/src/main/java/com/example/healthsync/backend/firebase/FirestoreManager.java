package com.example.healthsync.backend.firebase;

import android.util.Log;

import com.example.healthsync.frontend.models.Challenge;
import com.example.healthsync.frontend.models.UserProfile;
import com.example.healthsync.frontend.models.UserRank;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

    public static void saveUser(String uid, UserProfile profile) {
        db.collection("users")
                .document(uid)
                .set(profile)
                .addOnSuccessListener(aVoid ->
                        System.out.println("User saved successfully"))
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public static void getUser(String uid, OnSuccessListener<DocumentSnapshot> listener) {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(listener)
                .addOnFailureListener(Throwable::printStackTrace);
    }

    public interface FirestoreCallback<T> {
        void onCallback(T result);
    }

    public static void joinChallenge(String uid, Challenge challenge, OnSuccessListener<Void> successListener) {
        Log.d("CHALLENGE_FIRESTORE_SAVE", "Joining challenge: " + challenge.getId());
        Map<String, Object> data = new HashMap<>();
        data.put("challengeId", challenge.getId());
        data.put("challengeName", challenge.getName());
        data.put("joinedAt", System.currentTimeMillis());
        data.put("status", "ACTIVE");
        data.put("progress", 0);
        data.put("target", challenge.getGoal());
        data.put("rewardPoints", challenge.getRewardPoints());
        data.put("type", challenge.getType());

        db.collection("users")
                .document(uid)
                .collection("joinedChallenges")
                .document(challenge.getId())
                .set(data)
                .addOnSuccessListener(aVoid -> {
                    Log.d("CHALLENGE_FIRESTORE_SAVE", "Successfully joined in Firestore");
                    postToCommunityFeed(uid, "joined the " + challenge.getName() + " challenge!");
                    if (successListener != null) successListener.onSuccess(null);
                })
                .addOnFailureListener(e -> Log.e("CHALLENGE_FIRESTORE_SAVE", "Error joining challenge", e));
    }

    public static void checkChallengeJoined(String uid, String challengeId, OnSuccessListener<Boolean> listener) {
        db.collection("users")
                .document(uid)
                .collection("joinedChallenges")
                .document(challengeId)
                .get()
                .addOnSuccessListener(doc -> listener.onSuccess(doc.exists()));
    }

    public static void updateChallengeProgress(String uid, String challengeId, int progress, boolean completed) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("currentProgress", progress);
        updates.put("completed", completed);

        db.collection("users")
                .document(uid)
                .collection("joinedChallenges")
                .document(challengeId)
                .update(updates);
    }

    public static void listenToJoinedChallenges(String uid, ChallengeListener listener) {
        db.collection("users")
                .document(uid)
                .collection("joinedChallenges")
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    if (value != null) {
                        List<Challenge> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value) {
                            Challenge c = new Challenge();
                            c.setId(doc.getString("challengeId"));
                            c.setName(doc.getString("challengeName"));
                            Long progress = doc.getLong("progress");
                            c.setCurrentProgress(progress != null ? progress.intValue() : 0);
                            Long target = doc.getLong("target");
                            c.setGoal(target != null ? target.intValue() : 0);
                            Long reward = doc.getLong("rewardPoints");
                            c.setRewardPoints(reward != null ? reward.intValue() : 0);
                            c.setType(doc.getString("type"));
                            c.setJoined(true);
                            c.setCompleted("COMPLETED".equals(doc.getString("status")));
                            list.add(c);
                        }
                        listener.onChallengesLoaded(list);
                    }
                });
    }

    public static void awardChallengeReward(String uid, int points) {
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Long current = doc.getLong("points");
                if (current == null) current = 0L;
                
                Long challengesDone = doc.getLong("challengesDone");
                if (challengesDone == null) challengesDone = 0L;

                Map<String, Object> updates = new HashMap<>();
                updates.put("points", current + points);
                updates.put("challengesDone", challengesDone + 1);

                db.collection("users").document(uid).update(updates);
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
                        Long done = doc.getLong("challengesDone");
                        String progress = (done != null ? done : 0) + " challenges completed";

                        if (name != null) {
                            ranks.add(new UserRank(rank++, name, pts != null ? pts.intValue() : 0, doc.getId(), progress));
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

    public static void saveMealPlan(com.example.healthsync.backend.data.local.MealPlanEntity entity, com.example.healthsync.backend.data.model.MealPlanModel model) {
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

    public static void saveSleepEntry(com.example.healthsync.backend.data.local.SleepEntryEntity entry) {
        String uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        db.collection("users")
                .document(uid)
                .collection("sleep_logs")
                .document(entry.getDate())
                .set(entry);
    }

    public static void saveMoodLog(com.example.healthsync.backend.data.local.MoodLog log) {
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
        db.collection("users")
                .document(uid)
                .collection("joinedChallenges")
                .whereEqualTo("type", type)
                .get()
                .addOnSuccessListener(query -> {
                    for (DocumentSnapshot doc : query) {
                        Challenge c = doc.toObject(Challenge.class);
                        if (c != null && !c.isCompleted()) {
                            boolean completed = progress >= c.getGoal();
                            updateChallengeProgress(uid, c.getId(), progress, completed);
                            if (completed) {
                                postToCommunityFeed(uid, "completed the " + c.getName() + " challenge!");
                                awardChallengeReward(uid, c.getRewardPoints());
                            }
                        }
                    }
                });
    }

    public static void syncStepData(String uid, int steps) {
        db.collection("users").document(uid).update("todaySteps", steps);
        syncChallengeProgress(uid, "steps", steps);
    }

    public static void postToCommunityFeed(String uid, String activityText) {
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                Map<String, Object> activity = new HashMap<>();
                activity.put("userName", name);
                activity.put("activityText", activityText);
                activity.put("timestamp", System.currentTimeMillis());
                db.collection("community_feed").add(activity);
            }
        });
    }

    public static void loadCommunityFeed(OnSuccessListener<List<com.example.healthsync.frontend.models.CommunityActivity>> listener) {
        db.collection("community_feed")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(query -> {
                    List<com.example.healthsync.frontend.models.CommunityActivity> list = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        String user = doc.getString("userName");
                        String text = doc.getString("activityText");
                        Long time = doc.getLong("timestamp");
                        if (user != null) {
                            list.add(new com.example.healthsync.frontend.models.CommunityActivity(user, text, time != null ? time : 0));
                        }
                    }
                    listener.onSuccess(list);
                });
    }

    public static void getGlobalStats(OnSuccessListener<Map<String, String>> listener) {
        db.collection("system").document("global_stats").get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Map<String, String> stats = new HashMap<>();
                stats.put("steps", doc.getString("totalSteps"));
                stats.put("sleep", doc.getString("avgSleep"));
                listener.onSuccess(stats);
            } else {
                listener.onSuccess(new HashMap<>());
            }
        });
    }
}
