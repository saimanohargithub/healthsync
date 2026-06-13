package com.example.healthsync.frontend.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.healthsync.R;
import com.example.healthsync.databinding.ActivitySignupBinding;
import com.google.firebase.auth.FirebaseAuth;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        setupLoginText();

        binding.btnSignup.setOnClickListener(v -> {

            String name = binding.etName.getText().toString().trim();
            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();
            String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

            if (!validate(name, email, password, confirmPassword)) {
                return;
            }

            binding.btnSignup.setEnabled(false);

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {

                        binding.btnSignup.setEnabled(true);

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    SignupActivity.this,
                                    "Account Created Successfully",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // Navigate to Wizard Screen (as requested)
                            startActivity(new Intent(SignupActivity.this, WizardActivity.class));
                            finish();

                        } else {

                            Toast.makeText(
                                    SignupActivity.this,
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Registration Failed",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        binding.tvLoginLink.setOnClickListener(v -> {
            finish(); // Returns to Login screen
        });
    }

    private void setupLoginText() {

        String fullText = getString(R.string.already_have_account_signin);

        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf("Sign In");

        if (start != -1) {
            spannable.setSpan(
                    new ForegroundColorSpan(
                            ContextCompat.getColor(
                                    this,
                                    R.color.cyan_glow
                            )
                    ),
                    start,
                    fullText.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }

        binding.tvLoginLink.setText(spannable);
    }

    private boolean validate(String name, String email, String password, String confirmPassword) {

        if (TextUtils.isEmpty(name)) {
            binding.etName.setError("Name is required");
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("Email is required");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("Password is required");
            return false;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("Minimum 6 characters");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            return false;
        }

        return true;
    }
}
