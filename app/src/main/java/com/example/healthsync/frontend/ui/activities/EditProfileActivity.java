package com.example.healthsync.frontend.ui.activities;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.healthsync.R;
import com.example.healthsync.frontend.firebase.FirestoreManager;
import com.example.healthsync.frontend.models.UserProfile;
import com.example.healthsync.frontend.utils.PreferenceManager;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;

public class EditProfileActivity extends AppCompatActivity {

    private EditText etName, etAge, etHeight, etWeight;
    private MaterialButtonToggleGroup toggleGender;
    private PreferenceManager prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        prefs = new PreferenceManager(this);
        initViews();
        loadCurrentData();

        findViewById(R.id.btn_save).setOnClickListener(v -> saveChanges());
        ((androidx.appcompat.widget.Toolbar)findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> finish());
    }

    private void initViews() {
        etName = findViewById(R.id.et_name);
        etAge = findViewById(R.id.et_age);
        etHeight = findViewById(R.id.et_height);
        etWeight = findViewById(R.id.et_weight);
        toggleGender = findViewById(R.id.toggle_gender);
    }

    private void loadCurrentData() {
        etName.setText(prefs.getUserName());
        etAge.setText(String.valueOf(prefs.getUserAge()));
        etHeight.setText(String.valueOf(prefs.getUserHeight()));
        etWeight.setText(String.valueOf(prefs.getUserWeight()));

        String gender = prefs.getUserGender();
        if ("Male".equalsIgnoreCase(gender)) {
            toggleGender.check(R.id.btn_male);
        } else if ("Female".equalsIgnoreCase(gender)) {
            toggleGender.check(R.id.btn_female);
        } else {
            toggleGender.check(R.id.btn_other);
        }
    }

    private void saveChanges() {
        String name = etName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String heightStr = etHeight.getText().toString().trim();
        String weightStr = etWeight.getText().toString().trim();

        if (name.isEmpty() || ageStr.isEmpty() || heightStr.isEmpty() || weightStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        final int age = Integer.parseInt(ageStr);
        final float height = Float.parseFloat(heightStr);
        final float weight = Float.parseFloat(weightStr);

        String genderTemp = "Other";
        int checkedId = toggleGender.getCheckedButtonId();
        if (checkedId == R.id.btn_male) genderTemp = "Male";
        else if (checkedId == R.id.btn_female) genderTemp = "Female";
        final String gender = genderTemp;

        // Update Preferences
        prefs.setUserName(name);
        prefs.setUserAge(age);
        prefs.setUserHeight(height);
        prefs.setUserWeight(weight);
        prefs.setUserGender(gender);

        // Update Firestore
        final String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            UserProfile profile = new UserProfile();
            profile.name = name;
            profile.age = age;
            profile.height = height;
            profile.weight = weight;
            profile.gender = gender;
            profile.waterGoal = prefs.getWaterGoal();

            FirestoreManager.saveUser(uid, profile);

            // Update Local Room DB for automatic refresh
            final String finalName = name;
            new Thread(() -> {
                com.example.healthsync.backend.data.local.HealthDao dao = 
                    com.example.healthsync.backend.data.local.MealDatabase.Companion.getDatabase(this).healthDao();
                com.example.healthsync.backend.data.local.UserEntity current = dao.getUser();
                
                com.example.healthsync.backend.data.local.UserEntity updated = 
                    new com.example.healthsync.backend.data.local.UserEntity(
                        uid, finalName,
                        current != null ? current.getEmail() : "", 
                        age, gender, height, weight, 
                        current != null ? current.getPoints() : 0, 
                        System.currentTimeMillis()
                    );
                dao.insertUser(updated);
            }).start();
        }

        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}
