package project.hahuynam.nhandiensocongtodien;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.LocaleList;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private PreviewView previewView; // Displays the camera preview
    private ImageView imagePlaceHolder; // To display the captured photo
    private ImageCapture imageCapture; // Handles the photo capture
    private ExecutorService cameraExecutor; // Manages background tasks
    private static final int CAMERA_PERMISSION_CODE = 100; // Request code for camera permission

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        previewView = findViewById(R.id.Frame_CameraX);
        imagePlaceHolder = findViewById(R.id.Frame_Image_PlaceHolder);

        // Initialize ExecutorService
        cameraExecutor = Executors.newSingleThreadExecutor();

        // Check for camera permissions and start camera
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            startCamera();
        } else {
            requestCameraPermission();
        }

        // Button for capturing photo
        Button captureButton = findViewById(R.id.NutNhan_ChupAnh);
        captureButton.setOnClickListener(v -> capturePhoto());

        // Button for language switching
        Button switchLanguageButton = findViewById(R.id.NutNhan_ChuyenDoiNgonNgu);
        switchLanguageButton.setOnClickListener(v -> showLanguageSelectionDialog());
    }

    private void showLanguageSelectionDialog() {
        String[] languages = {"Tiếng Việt", "English", "中文"};
        String[] languageCodes = {"vi", "en", "zh"}; // Language codes

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Language");
        builder.setSingleChoiceItems(languages, -1, (dialog, which) -> {
            setLocale(languageCodes[which]);
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        config.setLocales(new LocaleList(locale));

        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        recreate();
    }

    private void requestCameraPermission() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                Toast.makeText(this, "Camera permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startCamera() {
        // Get a CameraProvider instance
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                // Configure the preview use case
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                // Configure the image capture use case
                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();

                // Select back-facing camera
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();

                // Bind Preview and ImageCapture to lifecycle
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                Log.e("CameraX", "Error initializing CameraX: ", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void capturePhoto() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera chưa được thiết lập chính xác!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Define a directory for saving the image
        File photoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ImageSaved");
        if (!photoDir.exists() && !photoDir.mkdirs()) {
            Toast.makeText(this, "Không thể tạo thư mục lưu ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the image file
        String fileName = "Photo_" + System.currentTimeMillis() + ".jpg";
        File photoFile = new File(photoDir, fileName);

        // Output options for saving the photo
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        // Capture the image and save to the file
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() -> {
                            // Update the ImageView to show the captured photo
                            imagePlaceHolder.setImageURI(Uri.fromFile(photoFile));
                            imagePlaceHolder.setVisibility(View.VISIBLE); // Show the ImageView
                            // Keep PreviewView running (do NOT hide it)
                            Toast.makeText(MainActivity.this, "Ảnh đã lưu tại: " + photoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(MainActivity.this, "Lỗi khi chụp ảnh: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("ImageCapture", "Lỗi khi lưu ảnh: ", exception);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown(); // Clean up camera resources
    }
}
