//MainActivity.java
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
import android.content.Intent;
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
    private PreviewView previewView;
    private ImageView imagePlaceHolder;
    private ImageCapture imageCapture;
    private ExecutorService cameraExecutor;
    private Uri HinhAnh_DaChup;
    private static final int CAMERA_PERMISSION_CODE = 100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Hiển thị giao diện chính
        previewView = findViewById(R.id.Frame_CameraX); // Hiển thị camera
        imagePlaceHolder = findViewById(R.id.Frame_Image_PlaceHolder); // Hiển thị ảnh đã chụp
        cameraExecutor = Executors.newSingleThreadExecutor(); // Khởi tạo bộ xử lý CameraX
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Mo_Camera(); // Mở camera nếu đã được cấp quyền
        } else {
            YeuCau_QuyenCamera(); // Yêu cầu quyền truy cập camera
        }
        Button captureButton = findViewById(R.id.NutNhan_ChupAnh);
        captureButton.setOnClickListener(v -> Chup_Anh()); // Sự kiện chụp ảnh
        Button switchLanguageButton = findViewById(R.id.NutNhan_ChuyenDoiNgonNgu);
        switchLanguageButton.setOnClickListener(v -> LuaChon_NgonNgu()); // Sự kiện chuyển đổi ngôn ngữ
        Button NutNhan_TimKiem = findViewById(R.id.NutNhan_TimKiem);
        NutNhan_TimKiem.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent); // Mở SearchActivity
        });
    }
    private void LuaChon_NgonNgu() {
        String[] languages = {"Tiếng Việt", "English", "中文"};
        String[] languageCodes = {"vi", "en", "zh"}; // Language codes
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Language");
        builder.setSingleChoiceItems(languages, -1, (dialog, which) -> {
            NgonNgu_KhuVuc(languageCodes[which]);
            dialog.dismiss();
        });
        builder.create().show();
    }
    private void NgonNgu_KhuVuc(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        config.setLocales(new LocaleList(locale));
        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        recreate();
    }
    private void YeuCau_QuyenCamera() {
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
                Mo_Camera();
            } else {
                Toast.makeText(this, "Quyền camera bị từ chối!", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void Mo_Camera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder()
                        .setTargetRotation(previewView.getDisplay().getRotation())
                        .build();
                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);

            } catch (Exception e) {
                Log.e("CameraX", "Error initializing CameraX: ", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void Chup_Anh() {
        if (imageCapture == null) {
            Toast.makeText(this, "Camera chưa được thiết lập chính xác!", Toast.LENGTH_SHORT).show();
            return;
        }
        File photoDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "ImageSaved");
        if (!photoDir.exists() && !photoDir.mkdirs()) {
            Toast.makeText(this, "Không thể tạo thư mục lưu ảnh!", Toast.LENGTH_SHORT).show();
            return;
        }
        String fileName = "Photo_" + System.currentTimeMillis() + ".jpg";
        File photoFile = new File(photoDir, fileName);
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();
        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(() -> {
                            if (photoFile.exists()) {
                                HinhAnh_DaChup = Uri.fromFile(photoFile);
                                imagePlaceHolder.setImageURI(HinhAnh_DaChup);
                                imagePlaceHolder.setVisibility(View.VISIBLE);
                                Toast.makeText(MainActivity.this, "Ảnh đã lưu tại: " + photoFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
                                Log.d("HinhAnh_DaChup", "Đường dẫn ảnh đã chụp: " + HinhAnh_DaChup.toString());
                            } else {
                                Toast.makeText(MainActivity.this, "Không thể lưu ảnh - file không tồn tại!", Toast.LENGTH_SHORT).show();
                                Log.e("HinhAnh_DaChup", "File không tồn tại, không thể tạo URI!");
                            }
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
        if (isFinishing() && !cameraExecutor.isShutdown()) {
            cameraExecutor.shutdown(); // Chỉ tắt executor khi Activity thực sự bị hủy
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if (previewView != null) {
            Mo_Camera(); // Khởi động lại camera mỗi khi quay lại
        }
    }
}

