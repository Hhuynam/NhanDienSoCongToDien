//SearchActivity.java
package project.hahuynam.nhandiensocongtodien;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

public class SearchActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timkiem); // Sử dụng giao diện tìm kiếm
        Button buttonQuayLai = findViewById(R.id.NutNhan_QuayLai);
        buttonQuayLai.setOnClickListener(v -> finish()); // Quay lại MainActivity
    }
}
