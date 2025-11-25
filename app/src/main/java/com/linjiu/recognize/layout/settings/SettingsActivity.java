package com.linjiu.recognize.layout.settings;



import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.linjiu.recognize.R;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNightMode, switchNotify;
    private Button btnClearCache;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchNightMode = findViewById(R.id.switchNightMode);
        switchNotify = findViewById(R.id.switchNotify);
        btnClearCache = findViewById(R.id.btnClearCache);

        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "已开启夜间模式", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "已关闭夜间模式", Toast.LENGTH_SHORT).show();
            }
        });

        switchNotify.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                Toast.makeText(this, "已开启通知", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "已关闭通知", Toast.LENGTH_SHORT).show();
            }
        });

        btnClearCache.setOnClickListener(v -> {
            Toast.makeText(this, "缓存已清除", Toast.LENGTH_SHORT).show();
        });
    }
}