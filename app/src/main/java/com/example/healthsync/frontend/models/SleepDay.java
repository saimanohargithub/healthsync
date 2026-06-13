package com.example.healthsync.frontend.models;

public class SleepDay {
    private String day;
    private float hours;

    public SleepDay(String day, float hours) {
        this.day = day;
        this.hours = hours;
    }

    public String getDay() { return day; }
    public float getHours() { return hours; }
}
