package com.example.healthsync.frontend.models;

public class MoodEntry {
    private String emoji;
    private long timestamp;

    public MoodEntry(String emoji, long timestamp) {
        this.emoji = emoji;
        this.timestamp = timestamp;
    }

    public String getEmoji() { return emoji; }
    public long getTimestamp() { return timestamp; }
}
