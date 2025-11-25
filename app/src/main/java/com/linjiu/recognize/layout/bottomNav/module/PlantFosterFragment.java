package com.linjiu.recognize.layout.bottomNav.module;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.snackbar.Snackbar;
import com.linjiu.recognize.R;

public class PlantFosterFragment extends Fragment {

    private TextInputEditText etHeight, etMoisture, etTemperature;
    private TextInputLayout layoutHeight, layoutMoisture, layoutTemperature;
    private RadioGroup rgModes;
    private Button btnSave, btnReset;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_modules_foster, container, false);

        initViews(view);
        setupListeners();
        loadSavedSettings();

        return view;
    }

    private void initViews(View view) {
        // 绑定输入控件
        etHeight = view.findViewById(R.id.et_height);
        etMoisture = view.findViewById(R.id.et_moisture);
        etTemperature = view.findViewById(R.id.et_temperature);

        layoutHeight = view.findViewById(R.id.layout_height);
        layoutMoisture = view.findViewById(R.id.layout_moisture);
        layoutTemperature = view.findViewById(R.id.layout_temperature);

        rgModes = view.findViewById(R.id.rg_modes);
        btnSave = view.findViewById(R.id.btn_save);
        btnReset = view.findViewById(R.id.btn_reset);
    }

    private void setupListeners() {
        // 保存按钮点击事件
        btnSave.setOnClickListener(v -> {
            if (validateInputs()) {
                saveSettings();
                showSuccessAnimation(v);
            }
        });

        // 重置按钮点击事件
        btnReset.setOnClickListener(v -> {
            resetToDefaults();
            showResetAnimation(v);
        });

        // 模式选择监听
        rgModes.setOnCheckedChangeListener((group, checkedId) -> {
            updateModeDescription(checkedId);
        });

        // 输入验证监听
        setupInputValidation();
    }

    private void setupInputValidation() {
        etHeight.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateHeight();
            }
        });

        etMoisture.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateMoisture();
            }
        });

        etTemperature.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                validateTemperature();
            }
        });
    }

    private boolean validateInputs() {
        boolean isValid = true;

        if (!validateHeight()) isValid = false;
        if (!validateMoisture()) isValid = false;
        if (!validateTemperature()) isValid = false;

        return isValid;
    }

    private boolean validateHeight() {
        String heightStr = etHeight.getText().toString().trim();
        if (heightStr.isEmpty()) {
            layoutHeight.setError("请输入植物高度");
            return false;
        }

        try {
            float height = Float.parseFloat(heightStr);
            if (height <= 0 || height > 500) {
                layoutHeight.setError("高度应在 0-500cm 之间");
                return false;
            }
            layoutHeight.setError(null);
            return true;
        } catch (NumberFormatException e) {
            layoutHeight.setError("请输入有效的数字");
            return false;
        }
    }

    private boolean validateMoisture() {
        String moistureStr = etMoisture.getText().toString().trim();
        if (moistureStr.isEmpty()) {
            layoutMoisture.setError("请输入湿度值");
            return false;
        }

        try {
            int moisture = Integer.parseInt(moistureStr);
            if (moisture < 0 || moisture > 100) {
                layoutMoisture.setError("湿度应在 0-100% 之间");
                return false;
            }
            layoutMoisture.setError(null);
            return true;
        } catch (NumberFormatException e) {
            layoutMoisture.setError("请输入有效的数字");
            return false;
        }
    }

    private boolean validateTemperature() {
        String temperatureStr = etTemperature.getText().toString().trim();
        if (temperatureStr.isEmpty()) {
            layoutTemperature.setError("请输入温度值");
            return false;
        }

        try {
            float temperature = Float.parseFloat(temperatureStr);
            if (temperature < -10 || temperature > 50) {
                layoutTemperature.setError("温度应在 -10-50℃ 之间");
                return false;
            }
            layoutTemperature.setError(null);
            return true;
        } catch (NumberFormatException e) {
            layoutTemperature.setError("请输入有效的数字");
            return false;
        }
    }

    private void saveSettings() {
        String height = etHeight.getText().toString();
        String moisture = etMoisture.getText().toString();
        String temperature = etTemperature.getText().toString();

        int selectedModeId = rgModes.getCheckedRadioButtonId();
        String mode = getModeText(selectedModeId);

        // 这里可以保存到 SharedPreferences 或数据库
        // 目前只做演示
        String message = String.format("设置已保存:\n高度: %scm\n湿度: %s%%\n温度: %s℃\n模式: %s",
                height, moisture, temperature, mode);

        Snackbar.make(getView(), "设置保存成功！", Snackbar.LENGTH_LONG)
                .setAction("查看", v -> showSettingsSummary(message))
                .show();
    }

    private void resetToDefaults() {
        etHeight.setText("");
        etMoisture.setText("");
        etTemperature.setText("");
        rgModes.check(R.id.rb_natural);

        // 清除错误提示
        layoutHeight.setError(null);
        layoutMoisture.setError(null);
        layoutTemperature.setError(null);

        Snackbar.make(getView(), "已重置为默认设置", Snackbar.LENGTH_SHORT).show();
    }

    private void loadSavedSettings() {
        // 这里可以从 SharedPreferences 加载保存的设置
        // 目前设置一些默认值作为示例
        etHeight.setText("");
        etMoisture.setText("");
        etTemperature.setText("");
        rgModes.check(R.id.rb_natural);
    }

    private String getModeText(int selectedModeId) {
        if (selectedModeId == R.id.rb_fast) {
            return "快速生长";
        } else if (selectedModeId == R.id.rb_energy) {
            return "节能模式";
        } else {
            return "自然模式";
        }
    }

    private void updateModeDescription(int checkedId) {
        // 可以根据选择的模式更新UI提示
        String description = "";
        if (checkedId == R.id.rb_natural) {
            description = "自然模式已选择，将模拟自然环境条件";
        } else if (checkedId == R.id.rb_fast) {
            description = "快速生长模式已选择，请确保充足的养分供应";
        } else if (checkedId == R.id.rb_energy) {
            description = "节能模式已选择，将降低设备功耗";
        }

        if (getView() != null) {
            Snackbar.make(getView(), description, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showSettingsSummary(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }

    private void showSuccessAnimation(View view) {
        // 保存按钮成功动画
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.9f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.9f, 1.0f);
        scaleX.setDuration(200);
        scaleY.setDuration(200);
        scaleX.start();
        scaleY.start();
    }

    private void showResetAnimation(View view) {
        // 重置按钮动画
        ObjectAnimator rotation = ObjectAnimator.ofFloat(view, "rotation", 0f, 360f);
        rotation.setDuration(500);
        rotation.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 页面恢复时可以刷新植物状态
        updatePlantStatus();
    }

    private void updatePlantStatus() {
        // 这里可以根据实际的植物传感器数据更新状态
        // 目前只是示例代码
    }
}