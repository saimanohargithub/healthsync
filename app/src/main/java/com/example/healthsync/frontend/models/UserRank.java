package com.example.healthsync.frontend.models;

public class UserRank {
    private int rank;
    private String name;
    private int points;
    private String uid;
    private String challengeProgress;

    public UserRank() {}

    public UserRank(int rank, String name, int points, String uid) {
        this(rank, name, points, uid, "0 challenges");
    }

    public UserRank(int rank, String name, int points, String uid, String challengeProgress) {
        this.rank = rank;
        this.name = name;
        this.points = points;
        this.uid = uid;
        this.challengeProgress = challengeProgress;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getChallengeProgress() { return challengeProgress; }
    public void setChallengeProgress(String challengeProgress) { this.challengeProgress = challengeProgress; }
}
