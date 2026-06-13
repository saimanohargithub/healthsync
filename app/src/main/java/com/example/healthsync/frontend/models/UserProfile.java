package com.example.healthsync.frontend.models;

import java.util.List;

public class UserProfile {

    public String name;
    public int age;
    public String gender;

    public float height;
    public float weight;

    public String goal;
    public String activityLevel;

    public float waterGoal;
    public float sleepGoal;

    public List<String> medicalConditions;

    public UserProfile() {
    }
}
