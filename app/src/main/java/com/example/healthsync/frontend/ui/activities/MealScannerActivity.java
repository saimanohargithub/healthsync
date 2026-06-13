package com.example.healthsync.frontend.ui.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.healthsync.databinding.ActivityMealScannerBinding;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MealScannerActivity extends AppCompatActivity {

    private ActivityMealScannerBinding binding;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;

    private int flashMode = ImageCapture.FLASH_MODE_OFF;

    private static final String[] REQUIRED_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private static final int REQUEST_CODE_PERMISSIONS = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMealScannerBinding.inflate(
                getLayoutInflater()
        );

        setContentView(binding.getRoot());

        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    REQUIRED_PERMISSIONS,
                    REQUEST_CODE_PERMISSIONS
            );
        }

        setupListeners();

        cameraExecutor =
                Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setupListeners() {

        binding.btnBack.setOnClickListener(
                v -> finish()
        );

        binding.btnCapture.setOnClickListener(v -> {

            binding.btnCapture.animate()
                    .scaleX(0.9f)
                    .scaleY(0.9f)
                    .setDuration(100)
                    .withEndAction(() -> {

                        binding.btnCapture.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(100);

                        takePhoto();
                    });
        });

        binding.btnFlash.setOnClickListener(v -> {

            flashMode =
                    flashMode == ImageCapture.FLASH_MODE_OFF
                            ? ImageCapture.FLASH_MODE_ON
                            : ImageCapture.FLASH_MODE_OFF;

            startCamera();

            Toast.makeText(
                    this,
                    "Flash: " +
                            (flashMode ==
                                    ImageCapture.FLASH_MODE_ON
                                    ? "ON"
                                    : "OFF"),
                    Toast.LENGTH_SHORT
            ).show();
        });

        binding.btnGallery.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Gallery picker coming soon",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    private void startCamera() {

        ListenableFuture<ProcessCameraProvider>
                cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {

            try {

                ProcessCameraProvider cameraProvider =
                        cameraProviderFuture.get();

                Preview preview =
                        new Preview.Builder().build();

                preview.setSurfaceProvider(
                        binding.previewView
                                .getSurfaceProvider()
                );

                imageCapture =
                        new ImageCapture.Builder()
                                .setFlashMode(flashMode)
                                .build();

                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();

                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException |
                     InterruptedException e) {

                Log.e(
                        "MealScanner",
                        "Camera initialization failed",
                        e
                );
            }

        }, ContextCompat.getMainExecutor(this));
    }

    private void takePhoto() {

        if (imageCapture == null) {
            return;
        }

        File photoFile = new File(
                getCacheDir(),
                new SimpleDateFormat(
                        "yyyy-MM-dd-HH-mm-ss-SSS",
                        Locale.US
                ).format(System.currentTimeMillis())
                        + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions
                        .Builder(photoFile)
                        .build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),

                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults
                                    outputFileResults
                    ) {

                        Intent intent =
                                new Intent(
                                        MealScannerActivity.this,
                                        MealScanningActivity.class
                                );

                        intent.putExtra(
                                "image_path",
                                photoFile.getAbsolutePath()
                        );

                        startActivity(intent);
                        finish();
                    }

                    @Override
                    public void onError(
                            @NonNull ImageCaptureException exception
                    ) {

                        Log.e(
                                "MealScanner",
                                "Photo capture failed",
                                exception
                        );

                        Toast.makeText(
                                MealScannerActivity.this,
                                "Failed to capture image",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private boolean allPermissionsGranted() {

        for (String permission :
                REQUIRED_PERMISSIONS) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    permission
            ) != PackageManager.PERMISSION_GRANTED) {

                return false;
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode ==
                REQUEST_CODE_PERMISSIONS) {

            if (allPermissionsGranted()) {

                startCamera();

            } else {

                Toast.makeText(
                        this,
                        "Camera permission required",
                        Toast.LENGTH_SHORT
                ).show();

                finish();
            }
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (cameraExecutor != null) {
            cameraExecutor.shutdown();
        }
    }
}
