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
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SoilMoistureFragment extends Fragment {

    private TextView detailTitle;
    private TextView currentValue;
    private TextView statusText;
    private TextView descriptionText;
    private TextView lastUpdateTime;
    private TextView nextIrrigationTime;
    private TextView nextIrrigationDate;
    private TextView irrigationAmount;

    private ImageView backButton, refreshButton;
    private ImageView dropletContainer;
    private ImageView moistureIndicator;
    private View statusIndicator;
    private View waveEffect1, waveEffect2, waveEffect3;
    private View waterLevel;

    private CardView historyCard, settingsCard;
    private CardView irrigationCard, alertCard, exportCard;

    private Handler handler = new Handler();
    private Random random = new Random();

    private int currentMoisture = 45;

    // 水波动画
    private ObjectAnimator wave1Animator, wave2Animator, wave3Animator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.devices_soilmoisture_fragment, container, false);

        initViews(view);
        setupClickListeners();
        updateData();
        startAnimations();
        startAutoRefresh();

        return view;
    }

    private void initViews(View view) {
        // 顶部控件
        backButton = view.findViewById(R.id.backButton);
        refreshButton = view.findViewById(R.id.refreshButton);
        detailTitle = view.findViewById(R.id.detailTitle);

        // 水滴和水波效果
        dropletContainer = view.findViewById(R.id.dropletContainer);
        waveEffect1 = view.findViewById(R.id.waveEffect1);
        waveEffect2 = view.findViewById(R.id.waveEffect2);
        waveEffect3 = view.findViewById(R.id.waveEffect3);
        waterLevel = view.findViewById(R.id.waterLevel);

        // 数值显示
        currentValue = view.findViewById(R.id.currentValue);
        statusText = view.findViewById(R.id.statusText);
        descriptionText = view.findViewById(R.id.descriptionText);

        // 状态指示器
        statusIndicator = view.findViewById(R.id.statusIndicator);
        lastUpdateTime = view.findViewById(R.id.lastUpdateTime);
        moistureIndicator = view.findViewById(R.id.moistureIndicator);

        // 灌溉信息
        nextIrrigationTime = view.findViewById(R.id.nextIrrigationTime);
        nextIrrigationDate = view.findViewById(R.id.nextIrrigationDate);
        irrigationAmount = view.findViewById(R.id.irrigationAmount);

        // 功能卡片
        historyCard = view.findViewById(R.id.historyCard);
        settingsCard = view.findViewById(R.id.settingsCard);
        irrigationCard = view.findViewById(R.id.irrigationCard);
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
            Toast.makeText(getContext(), "查看土壤湿度历史", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到历史数据页面
        });

        settingsCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "传感器设置", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到传感器设置页面
        });

        irrigationCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "智能灌溉控制", Toast.LENGTH_SHORT).show();
            // TODO: 打开灌溉控制界面
        });

        alertCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "湿度预警设置", Toast.LENGTH_SHORT).show();
            // TODO: 打开预警设置对话框
        });

        exportCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "导出湿度报告", Toast.LENGTH_SHORT).show();
            // TODO: 生成并导出报告
        });
    }

    private void startAnimations() {
        // 水波1动画 - 缩放和透明度
        wave1Animator = ObjectAnimator.ofFloat(waveEffect1, "scaleX", 1.0f, 1.2f, 1.0f);
        wave1Animator.setDuration(3000);
        wave1Animator.setRepeatCount(ValueAnimator.INFINITE);
        wave1Animator.setInterpolator(new AccelerateDecelerateInterpolator());
        wave1Animator.start();

        ObjectAnimator wave1ScaleY = ObjectAnimator.ofFloat(waveEffect1, "scaleY", 1.0f, 1.2f, 1.0f);
        wave1ScaleY.setDuration(3000);
        wave1ScaleY.setRepeatCount(ValueAnimator.INFINITE);
        wave1ScaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        wave1ScaleY.start();

        // 水波2动画
        wave2Animator = ObjectAnimator.ofFloat(waveEffect2, "scaleX", 1.0f, 1.3f, 1.0f);
        wave2Animator.setDuration(3500);
        wave2Animator.setRepeatCount(ValueAnimator.INFINITE);
        wave2Animator.setInterpolator(new AccelerateDecelerateInterpolator());
        wave2Animator.start();

        ObjectAnimator wave2ScaleY = ObjectAnimator.ofFloat(waveEffect2, "scaleY", 1.0f, 1.3f, 1.0f);
        wave2ScaleY.setDuration(3500);
        wave2ScaleY.setRepeatCount(ValueAnimator.INFINITE);
        wave2ScaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        wave2ScaleY.start();

        // 水波3动画
        wave3Animator = ObjectAnimator.ofFloat(waveEffect3, "scaleX", 1.0f, 1.4f, 1.0f);
        wave3Animator.setDuration(4000);
        wave3Animator.setRepeatCount(ValueAnimator.INFINITE);
        wave3Animator.setInterpolator(new AccelerateDecelerateInterpolator());
        wave3Animator.start();

        ObjectAnimator wave3ScaleY = ObjectAnimator.ofFloat(waveEffect3, "scaleY", 1.0f, 1.4f, 1.0f);
        wave3ScaleY.setDuration(4000);
        wave3ScaleY.setRepeatCount(ValueAnimator.INFINITE);
        wave3ScaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        wave3ScaleY.start();

        // 水滴轻微浮动动画
        ObjectAnimator dropletFloat = ObjectAnimator.ofFloat(dropletContainer, "translationY", 0f, -10f, 0f);
        dropletFloat.setDuration(2000);
        dropletFloat.setRepeatCount(ValueAnimator.INFINITE);
        dropletFloat.setInterpolator(new AccelerateDecelerateInterpolator());
        dropletFloat.start();
    }

    private void updateData() {
        // 模拟传感器数据（可替换为真实传感器）
        currentMoisture = getSimulatedMoisture();

        // 更新湿度显示
        currentValue.setText(String.valueOf(currentMoisture));

        // 更新状态
        updateMoistureStatus(currentMoisture);

        // 更新水位显示
        updateWaterLevel(currentMoisture);

        // 更新指示器位置
        updateMoistureIndicator(currentMoisture);

        // 更新建议
        updateIrrigationAdvice(currentMoisture);

        // 更新下次灌溉时间
        updateNextIrrigation(currentMoisture);

        // 更新时间
        updateLastUpdateTime();
    }

    private int getSimulatedMoisture() {
        // 模拟土壤湿度变化（20-80%）
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        int hour = Integer.parseInt(sdf.format(new Date()));

        int baseMoisture;
        if (hour >= 6 && hour < 10) {
            // 早上蒸发，湿度降低
            baseMoisture = 55 - (hour - 6) * 3;
        } else if (hour >= 10 && hour < 18) {
            // 白天持续蒸发
            baseMoisture = 40 + random.nextInt(10);
        } else if (hour >= 18 && hour < 22) {
            // 傍晚湿度回升
            baseMoisture = 45 + (hour - 18) * 2;
        } else {
            // 夜间保持
            baseMoisture = 50 + random.nextInt(10);
        }

        return Math.max(20, Math.min(80, baseMoisture + random.nextInt(5)));
    }

    private void updateMoistureStatus(int moisture) {
        String status;
        int color;
        int bgRes;
        int iconRes;

        if (moisture < 20) {
            status = "严重干旱";
            color = getResources().getColor(R.color.status_danger);
            bgRes = R.drawable.bg_status_tag_red;
            iconRes = R.drawable.ic_warning;
            statusIndicator.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_danger))
            );
        } else if (moisture < 40) {
            status = "偏干";
            color = getResources().getColor(R.color.status_warning);
            bgRes = R.drawable.bg_status_tag_orange;
            iconRes = R.drawable.ic_warning;
            statusIndicator.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_warning))
            );
        } else if (moisture <= 70) {
            status = "湿度适宜";
            color = getResources().getColor(R.color.status_success);
            bgRes = R.drawable.bg_status_tag_green;
            iconRes = R.drawable.ic_check_circle;
            statusIndicator.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_success))
            );
        } else if (moisture <= 85) {
            status = "偏湿";
            color = getResources().getColor(R.color.status_info);
            bgRes = R.drawable.bg_status_tag_blue;
            iconRes = R.drawable.ic_warning;
            statusIndicator.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_info))
            );
        } else {
            status = "过湿";
            color = getResources().getColor(R.color.status_danger);
            bgRes = R.drawable.bg_status_tag_red;
            iconRes = R.drawable.ic_warning;
            statusIndicator.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.status_danger))
            );
        }

        statusText.setText(status);
        statusText.setTextColor(color);
        statusText.setBackgroundResource(bgRes);
        statusText.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
    }

    private void updateWaterLevel(int moisture) {
        // 根据湿度调整水位高度（动画）
        if (waterLevel != null) {
            float targetHeight = (moisture / 100f) * 80; // 最大80dp
            ViewGroup.LayoutParams params = waterLevel.getLayoutParams();

            ValueAnimator animator = ValueAnimator.ofInt(params.height, (int) targetHeight);
            animator.setDuration(500);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                params.height = (int) animation.getAnimatedValue();
                waterLevel.setLayoutParams(params);
            });
            animator.start();
        }
    }

    private void updateMoistureIndicator(int moisture) {
        // 湿度指示器位置（0-100%映射到进度条）
        float percentage = moisture / 100f;

        if (moistureIndicator != null && moistureIndicator.getParent() != null) {
            View parent = (View) moistureIndicator.getParent();
            parent.post(() -> {
                float targetX = parent.getWidth() * percentage - moistureIndicator.getWidth() / 2;

                ValueAnimator animator = ValueAnimator.ofFloat(
                        moistureIndicator.getTranslationX(),
                        targetX
                );
                animator.setDuration(500);
                animator.setInterpolator(new AccelerateDecelerateInterpolator());
                animator.addUpdateListener(animation -> {
                    float value = (float) animation.getAnimatedValue();
                    moistureIndicator.setTranslationX(value);
                });
                animator.start();
            });
        }
    }

    private void updateIrrigationAdvice(int moisture) {
        StringBuilder advice = new StringBuilder();

        if (moisture < 20) {
            advice.append("土壤严重缺水！请立即进行灌溉，建议灌水量3-4L/m²。");
        } else if (moisture < 40) {
            advice.append("土壤偏干，需要适量灌溉。建议灌水量2-3L/m²，确保水分渗透到根系深度。");
        } else if (moisture <= 70) {
            advice.append("当前土壤湿度适中，水分充足。植物根系可以正常吸收水分和养分，暂时无需灌溉。");
        } else if (moisture <= 85) {
            advice.append("土壤湿度偏高，暂停灌溉。建议加强通风排湿，避免根系缺氧。");
        } else {
            advice.append("土壤过湿，停止灌溉！注意排水，防止根系腐烂和病害发生。");
        }

        descriptionText.setText(advice.toString());
    }

    private void updateNextIrrigation(int moisture) {
        Calendar calendar = Calendar.getInstance();

        int hoursToNext;
        String amount;

        if (moisture < 20) {
            hoursToNext = 0;
            amount = "3-4L/m²";
            nextIrrigationTime.setText("立即灌溉");
            nextIrrigationTime.setTextColor(getResources().getColor(R.color.status_danger));
        } else if (moisture < 40) {
            hoursToNext = 6;
            amount = "2-3L/m²";
            nextIrrigationTime.setText(hoursToNext + "小时后");
            nextIrrigationTime.setTextColor(getResources().getColor(R.color.status_warning));
        } else if (moisture <= 70) {
            hoursToNext = 24;
            amount = "2L/m²";
            nextIrrigationTime.setText(hoursToNext + "小时后");
            nextIrrigationTime.setTextColor(getResources().getColor(R.color.status_info));
        } else {
            hoursToNext = 48;
            amount = "0L/m²";
            nextIrrigationTime.setText("暂不需要");
            nextIrrigationTime.setTextColor(getResources().getColor(R.color.status_success));
        }

        calendar.add(Calendar.HOUR_OF_DAY, hoursToNext);
        SimpleDateFormat sdf = new SimpleDateFormat("MM月dd日 HH:mm", Locale.getDefault());
        String dateStr = sdf.format(calendar.getTime());

        if (hoursToNext == 0) {
            nextIrrigationDate.setText("紧急");
        } else if (hoursToNext <= 24) {
            nextIrrigationDate.setText("今天 " + new SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.getTime()));
        } else {
            nextIrrigationDate.setText(dateStr);
        }

        irrigationAmount.setText(amount);
    }

    private void updateLastUpdateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        lastUpdateTime.setText("实时监测 " + currentTime);
    }

    private void refreshData() {
        Toast.makeText(getContext(), "正在刷新数据...", Toast.LENGTH_SHORT).show();

        // 刷新按钮旋转动画
        refreshButton.animate()
                .rotation(refreshButton.getRotation() + 360f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    updateData();
                    Toast.makeText(getContext(), "数据已更新", Toast.LENGTH_SHORT).show();
                })
                .start();

        // 水滴跳动动画
        dropletContainer.animate()
                .scaleX(1.3f)
                .scaleY(1.3f)
                .setDuration(250)
                .withEndAction(() -> {
                    dropletContainer.animate()
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
        if (wave1Animator != null && !wave1Animator.isRunning()) {
            wave1Animator.start();
        }
        if (wave2Animator != null && !wave2Animator.isRunning()) {
            wave2Animator.start();
        }
        if (wave3Animator != null && !wave3Animator.isRunning()) {
            wave3Animator.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (wave1Animator != null) {
            wave1Animator.pause();
        }
        if (wave2Animator != null) {
            wave2Animator.pause();
        }
        if (wave3Animator != null) {
            wave3Animator.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (wave1Animator != null) {
            wave1Animator.cancel();
        }
        if (wave2Animator != null) {
            wave2Animator.cancel();
        }
        if (wave3Animator != null) {
            wave3Animator.cancel();
        }
    }
}