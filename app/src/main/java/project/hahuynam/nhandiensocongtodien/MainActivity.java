package project.hahuynam.nhandiensocongtodien;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.Button;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button switchLanguageButton = findViewById(R.id.NutNhan_ChuyenDoiNgonNgu);

        // Hiển thị danh sách chọn ngôn ngữ khi nhấn nút
        switchLanguageButton.setOnClickListener(v -> showLanguageSelectionDialog());
    }

    private void showLanguageSelectionDialog() {
        // Danh sách các ngôn ngữ
        String[] languages = {"Tiếng Việt", "English", "中文"};
        String[] languageCodes = {"vi", "en", "zh"}; // Mã ngôn ngữ tương ứng

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Chọn ngôn ngữ");
        builder.setSingleChoiceItems(languages, -1, (dialog, which) -> {
            String selectedLanguageCode = languageCodes[which];
            setLocale(selectedLanguageCode); // Chuyển đổi ngôn ngữ
            dialog.dismiss(); // Đóng dialog sau khi chọn
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);

        Configuration config = getResources().getConfiguration();
        config.setLocale(locale);
        config.setLocales(new android.os.LocaleList(locale));

        Resources resources = getResources();
        resources.updateConfiguration(config, resources.getDisplayMetrics());
        recreate(); // Làm mới Activity để áp dụng ngôn ngữ mới
    }
}
