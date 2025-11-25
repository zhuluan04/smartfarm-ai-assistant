package com.linjiu.recognize.layout.bottomNav.monitor.devices;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class TempHumidityFragment extends Fragment {

    private TextView detailTitle;
    private TextView temperatureValue, humidityValue;
    private TextView tempStatus, humidityStatus;
    private TextView overallStatus;
    private TextView descriptionText;
    private TextView lastUpdateTime;
    private TextView comfortScore;

    private ImageView backButton, refreshButton;
    private ImageView tempIcon, humidityIcon;
    private ImageView tempIndicator, humidityIndicator;
    private View statusIndicator;

    private ProgressBar comfortProgress;

    private CardView historyCard, settingsCard;
    private CardView alertCard, exportCard;

    private Handler handler = new Handler();
    private Random random = new Random();

    private float currentTemperature = 25.3f;
    private int currentHumidity = 65;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.devices_temphumidity_fragment, container, false);

        initViews(view);
        setupClickListeners();
        updateData();
        startAutoRefresh();

        return view;
    }

    private void initViews(View view) {
        // 顶部控件
        backButton = view.findViewById(R.id.backButton);
        refreshButton = view.findViewById(R.id.refreshButton);
        detailTitle = view.findViewById(R.id.detailTitle);

        // 温度湿度显示
        temperatureValue = view.findViewById(R.id.temperatureValue);
        humidityValue = view.findViewById(R.id.humidityValue);
        tempStatus = view.findViewById(R.id.tempStatus);
        humidityStatus = view.findViewById(R.id.humidityStatus);

        // 图标
        tempIcon = view.findViewById(R.id.tempIcon);
        humidityIcon = view.findViewById(R.id.humidityIcon);

        // 状态指示器
        statusIndicator = view.findViewById(R.id.statusIndicator);
        overallStatus = view.findViewById(R.id.overallStatus);
        lastUpdateTime = view.findViewById(R.id.lastUpdateTime);

        // 舒适度
        comfortProgress = view.findViewById(R.id.comfortProgress);
        comfortScore = view.findViewById(R.id.comfortScore);

        // 范围指示器
        tempIndicator = view.findViewById(R.id.tempIndicator);
        humidityIndicator = view.findViewById(R.id.humidityIndicator);

        // 描述文本
        descriptionText = view.findViewById(R.id.descriptionText);

        // 功能卡片
        historyCard = view.findViewById(R.id.historyCard);
        settingsCard = view.findViewById(R.id.settingsCard);
        alertCard = view.findViewById(R.id.alertCard);
        exportCard = view.findViewById(R.id.exportCard);
    }

    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        refreshButton.setOnClickListener(v -> {
            refreshData();
        });

        historyCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "查看温湿度历史数据", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到历史数据页面
        });

        settingsCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "传感器设置", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到传感器设置页面
        });

        alertCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "温湿度预警设置", Toast.LENGTH_SHORT).show();
            // TODO: 打开预警设置对话框
        });

        exportCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "导出环境报告", Toast.LENGTH_SHORT).show();
            // TODO: 生成并导出报告
        });
    }

    private void updateData() {
        // 模拟传感器数据（可替换为真实传感器读数）
        currentTemperature = getSimulatedTemperature();
        currentHumidity = getSimulatedHumidity();

        // 更新温度显示
        DecimalFormat df = new DecimalFormat("#0.0");
        temperatureValue.setText(df.format(currentTemperature));

        // 更新湿度显示
        humidityValue.setText(String.valueOf(currentHumidity));

        // 更新温度状态
        updateTemperatureStatus(currentTemperature);

        // 更新湿度状态
        updateHumidityStatus(currentHumidity);

        // 更新整体状态
        updateOverallStatus(currentTemperature, currentHumidity);

        // 更新舒适度
        updateComfortIndex(currentTemperature, currentHumidity);

        // 更新范围指示器
        updateRangeIndicators(currentTemperature, currentHumidity);

        // 更新建议
        updateSuggestions(currentTemperature, currentHumidity);

        // 更新时间
        updateLastUpdateTime();
    }

    private float getSimulatedTemperature() {
        // 模拟温度在18-30度之间波动
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        int hour = Integer.parseInt(sdf.format(new Date()));

        float baseTemp;
        if (hour >= 6 && hour < 12) {
            // 上午逐渐升温
            baseTemp = 20 + (hour - 6) * 1.5f;
        } else if (hour >= 12 && hour < 16) {
            // 下午高温
            baseTemp = 26 + random.nextFloat() * 2;
        } else if (hour >= 16 && hour < 22) {
            // 傍晚降温
            baseTemp = 28 - (hour - 16) * 1.2f;
        } else {
            // 夜间低温
            baseTemp = 18 + random.nextFloat() * 2;
        }

        // 添加随机波动
        return baseTemp + (random.nextFloat() - 0.5f);
    }

    private int getSimulatedHumidity() {
        // 模拟湿度在40-80%之间
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        int hour = Integer.parseInt(sdf.format(new Date()));

        int baseHumidity;
        if (hour >= 6 && hour < 12) {
            // 上午湿度降低
            baseHumidity = 70 - (hour - 6) * 2;
        } else if (hour >= 12 && hour < 18) {
            // 下午湿度较低
            baseHumidity = 55 + random.nextInt(10);
        } else {
            // 晚上湿度回升
            baseHumidity = 65 + random.nextInt(10);
        }

        return Math.max(40, Math.min(80, baseHumidity));
    }

    private void updateTemperatureStatus(float temp) {
        String status;
        int color;
        int bgRes;

        if (temp < 15) {
            status = "偏低";
            color = getResources().getColor(R.color.status_info);
            bgRes = R.drawable.bg_status_tag_blue;
        } else if (temp < 18) {
            status = "较低";
            color = getResources().getColor(R.color.status_warning);
            bgRes = R.drawable.bg_status_tag_orange;
        } else if (temp <= 28) {
            status = "适宜";
            color = getResources().getColor(R.color.status_success);
            bgRes = R.drawable.bg_status_tag_green;
        } else if (temp <= 32) {
            status = "偏高";
            color = getResources().getColor(R.color.status_warning);
            bgRes = R.drawable.bg_status_tag_orange;
        } else {
            status = "过高";
            color = getResources().getColor(R.color.status_danger);
            bgRes = R.drawable.bg_status_tag_red;
        }

        tempStatus.setText(status);
        tempStatus.setTextColor(color);
        tempStatus.setBackgroundResource(bgRes);
    }

    private void updateHumidityStatus(int humidity) {
        String status;
        int color;
        int bgRes;

        if (humidity < 40) {
            status = "干燥";
            color = getResources().getColor(R.color.status_warning);
            bgRes = R.drawable.bg_status_tag_orange;
        } else if (humidity < 60) {
            status = "适中";
            color = getResources().getColor(R.color.status_info);
            bgRes = R.drawable.bg_status_tag_blue;
        } else if (humidity <= 75) {
            status = "正常";
            color = getResources().getColor(R.color.status_success);
            bgRes = R.drawable.bg_status_tag_green;
        } else if (humidity <= 85) {
            status = "偏高";
            color = getResources().getColor(R.color.status_warning);
            bgRes = R.drawable.bg_status_tag_orange;
        } else {
            status = "潮湿";
            color = getResources().getColor(R.color.status_danger);
            bgRes = R.drawable.bg_status_tag_red;
        }

        humidityStatus.setText(status);
        humidityStatus.setTextColor(color);
        humidityStatus.setBackgroundResource(bgRes);
    }

    private void updateOverallStatus(float temp, int humidity) {
        boolean tempOk = temp >= 18 && temp <= 28;
        boolean humidityOk = humidity >= 60 && humidity <= 75;

        String status;
        int color;
        int indicatorColor;

        if (tempOk && humidityOk) {
            status = "理想";
            color = getResources().getColor(R.color.status_success);
            indicatorColor = getResources().getColor(R.color.status_success);
        } else if (!tempOk || !humidityOk) {
            status = "一般";
            color = getResources().getColor(R.color.status_warning);
            indicatorColor = getResources().getColor(R.color.status_warning);
        } else {
            status = "异常";
            color = getResources().getColor(R.color.status_danger);
            indicatorColor = getResources().getColor(R.color.status_danger);
        }

        overallStatus.setText(status);
        overallStatus.setTextColor(color);
        statusIndicator.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(indicatorColor)
        );
    }

    private void updateComfortIndex(float temp, int humidity) {
        // 计算舒适度指数 (简化算法)
        int score;

        if (temp >= 20 && temp <= 26 && humidity >= 50 && humidity <= 70) {
            score = 90 + random.nextInt(10); // 90-100
        } else if (temp >= 18 && temp <= 28 && humidity >= 45 && humidity <= 75) {
            score = 75 + random.nextInt(15); // 75-90
        } else if (temp >= 15 && temp <= 30 && humidity >= 40 && humidity <= 80) {
            score = 60 + random.nextInt(15); // 60-75
        } else {
            score = 40 + random.nextInt(20); // 40-60
        }

        comfortScore.setText(String.valueOf(score));

        // 更新颜色
        int scoreColor;
        if (score >= 85) {
            scoreColor = getResources().getColor(R.color.status_success);
        } else if (score >= 70) {
            scoreColor = getResources().getColor(R.color.status_info);
        } else if (score >= 55) {
            scoreColor = getResources().getColor(R.color.status_warning);
        } else {
            scoreColor = getResources().getColor(R.color.status_danger);
        }
        comfortScore.setTextColor(scoreColor);

        // 动画更新进度条
        ObjectAnimator progressAnimator = ObjectAnimator.ofInt(comfortProgress, "progress", score);
        progressAnimator.setDuration(1000);
        progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        progressAnimator.start();
    }

    private void updateRangeIndicators(float temp, int humidity) {
        // 温度指示器位置 (10-35度映射到0-100%)
        float tempPercentage = (temp - 10) / 25f;
        tempPercentage = Math.max(0f, Math.min(1f, tempPercentage));
        updateIndicatorPosition(tempIndicator, tempPercentage);

        // 湿度指示器位置 (30-90%映射到0-100%)
        float humidityPercentage = (humidity - 30) / 60f;
        humidityPercentage = Math.max(0f, Math.min(1f, humidityPercentage));
        updateIndicatorPosition(humidityIndicator, humidityPercentage);
    }

    private void updateIndicatorPosition(ImageView indicator, float percentage) {
        if (indicator != null && indicator.getParent() != null) {
            View parent = (View) indicator.getParent();
            parent.post(() -> {
                float targetX = parent.getWidth() * percentage - indicator.getWidth() / 2;

                ValueAnimator animator = ValueAnimator.ofFloat(indicator.getTranslationX(), targetX);
                animator.setDuration(500);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    indicator.setTranslationX(value);
                });
                animator.start();
            });
        }
    }

    private void updateSuggestions(float temp, int humidity) {
        StringBuilder suggestions = new StringBuilder();

        if (temp >= 18 && temp <= 28) {
            suggestions.append("当前温度适宜");
        } else if (temp < 18) {
            suggestions.append("温度偏低，建议增加环境温度");
        } else {
            suggestions.append("温度偏高，建议降温或增加通风");
        }

        suggestions.append("，");

        if (humidity >= 60 && humidity <= 75) {
            suggestions.append("湿度理想");
        } else if (humidity < 60) {
            suggestions.append("湿度偏低，建议增加湿度");
        } else {
            suggestions.append("湿度偏高，建议降湿或通风");
        }

        suggestions.append("。");

        if (temp >= 18 && temp <= 28 && humidity >= 60 && humidity <= 75) {
            suggestions.append("非常适合大多数植物生长。建议保持现有环境条件。");
        } else {
            suggestions.append("建议调整环境参数以达到最佳生长条件。");
        }

        descriptionText.setText(suggestions.toString());
    }

    private void updateLastUpdateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        lastUpdateTime.setText(currentTime);
    }

    private void refreshData() {
        Toast.makeText(getContext(), "正在刷新数据...", Toast.LENGTH_SHORT).show();

        // 刷新按钮动画
        refreshButton.animate()
                .rotation(refreshButton.getRotation() + 360f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    updateData();
                    Toast.makeText(getContext(), "数据已更新", Toast.LENGTH_SHORT).show();
                })
                .start();

        // 图标脉冲动画
        tempIcon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(250)
                .withEndAction(() -> {
                    tempIcon.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(250)
                            .start();
                })
                .start();

        humidityIcon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(250)
                .setStartDelay(100)
                .withEndAction(() -> {
                    humidityIcon.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(250)
                            .start();
                })
                .start();
    }

    private void startAutoRefresh() {
        // 每30秒自动刷新
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateData();
                handler.postDelayed(this, 30000);
            }
        }, 30000);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
    }
}