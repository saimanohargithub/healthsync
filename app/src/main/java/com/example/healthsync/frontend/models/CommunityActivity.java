package com.example.healthsync.frontend.models;

public class CommunityActivity {
    private String userName;
    private String activityText;
    private long timestamp;

    public CommunityActivity(String userName, String activityText, long timestamp) {
        this.userName = userName;
        this.activityText = activityText;
        this.timestamp = timestamp;
    }

    public String getUserName() { return userName; }
    public String getActivityText() { return activityText; }
    public long getTimestamp() { return timestamp; }
}
