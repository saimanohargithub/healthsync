package com.example.healthsync.frontend.models;

public class Challenge {
    private String id;
    private String name;
    private int currentProgress;
    private int goal;
    private int rewardPoints;
    private int participants;
    private boolean joined;
    private boolean completed;
    private String type; // hydration, sleep, calories, steps
    private long joinedAt;

    public Challenge() {}

    public Challenge(String id, String name, int goal, int rewardPoints, int participants, String type) {
        this.id = id;
        this.name = name;
        this.goal = goal;
        this.rewardPoints = rewardPoints;
        this.participants = participants;
        this.type = type;
        this.joined = false;
        this.completed = false;
        this.currentProgress = 0;
        this.joinedAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getJoinedAt() { return joinedAt; }
    public void setJoinedAt(long joinedAt) { this.joinedAt = joinedAt; }
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCurrentProgress() { return currentProgress; }
    public void setCurrentProgress(int currentProgress) { this.currentProgress = currentProgress; }
    public int getGoal() { return goal; }
    public void setGoal(int goal) { this.goal = goal; }
    public int getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(int rewardPoints) { this.rewardPoints = rewardPoints; }
    public int getParticipants() { return participants; }
    public void setParticipants(int participants) { this.participants = participants; }
    public boolean isJoined() { return joined; }
    public void setJoined(boolean joined) { this.joined = joined; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getPercentage() {
        if (goal == 0) return 0;
        int p = (int) ((currentProgress * 100.0f) / goal);
        return Math.min(p, 100);
    }
}
