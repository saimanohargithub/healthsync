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
import com.example.healthsync.databinding.ActivityLoginBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        setupSignUpText();

        binding.btnLogin.setOnClickListener(v -> {

            String email = binding.etEmail.getText().toString().trim();
            String password = binding.etPassword.getText().toString().trim();

            if (!validate(email, password)) {
                return;
            }

            binding.btnLogin.setEnabled(false);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {

                        binding.btnLogin.setEnabled(true);

                        if (task.isSuccessful()) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login Successful",
                                    Toast.LENGTH_SHORT
                            ).show();

                            startActivity(
                                    new Intent(
                                            LoginActivity.this,
                                            MainActivity.class
                                    )
                            );

                            finish();

                        } else {

                            Toast.makeText(
                                    LoginActivity.this,
                                    task.getException() != null
                                            ? task.getException().getMessage()
                                            : "Login Failed",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    });
        });

        binding.tvSignupLink.setOnClickListener(v -> 
            startActivity(new Intent(this, SignupActivity.class))
        );
    }

    private void setupSignUpText() {

        String fullText = getString(R.string.no_account);

        SpannableString spannable = new SpannableString(fullText);

        int start = fullText.indexOf("Sign Up");

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

        binding.tvSignupLink.setText(spannable);
    }

    private boolean validate(String email, String password) {

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

        return true;
    }
}
