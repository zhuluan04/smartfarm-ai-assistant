package com.linjiu.recognize.layout.bottomNav.monitor.devices;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
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

public class LightIntensityFragment extends Fragment {

    private TextView detailTitle;
    private TextView currentValue;
    private TextView statusText;
    private TextView descriptionText;
    private TextView lastUpdateTime;
    private ImageView backButton;
    private ImageView refreshButton;
    private ImageView sunIcon;
    private ImageView raysIcon;
    private ImageView intensityIndicator;
    private View statusIndicator;
    private View glowEffect1, glowEffect2, glowEffect3;

    private CardView historyCard;
    private CardView settingsCard;
    private CardView alertCard;
    private CardView compareCard;
    private CardView exportCard;

    private Handler handler = new Handler();
    private Random random = new Random();
    private int currentLuxValue = 15200;

    // 动画对象
    private ObjectAnimator sunRotationAnimator;
    private ObjectAnimator glowPulseAnimator;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.devices_light_detail_fragment, container, false);

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

        // 状态指示器
        statusIndicator = view.findViewById(R.id.statusIndicator);
        lastUpdateTime = view.findViewById(R.id.lastUpdateTime);

        // 太阳图标和光晕效果
        sunIcon = view.findViewById(R.id.sunIcon);
        raysIcon = view.findViewById(R.id.raysIcon);
        glowEffect1 = view.findViewById(R.id.glowEffect1);
        glowEffect2 = view.findViewById(R.id.glowEffect2);
        glowEffect3 = view.findViewById(R.id.glowEffect3);

        // 数值显示
        currentValue = view.findViewById(R.id.currentValue);
        statusText = view.findViewById(R.id.statusText);
        descriptionText = view.findViewById(R.id.descriptionText);

        // 强度指示器
        intensityIndicator = view.findViewById(R.id.intensityIndicator);

        // 功能卡片
        historyCard = view.findViewById(R.id.historyCard);
        settingsCard = view.findViewById(R.id.settingsCard);
        alertCard = view.findViewById(R.id.alertCard);
        compareCard = view.findViewById(R.id.compareCard);
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
            Toast.makeText(getContext(), "查看历史光照数据", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到历史数据页面，显示折线图
        });

        settingsCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "传感器设置", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到传感器配置页面
        });

        alertCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "设置光照预警阈值", Toast.LENGTH_SHORT).show();
            // TODO: 打开光照预警设置对话框
        });

        compareCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "多日光照对比", Toast.LENGTH_SHORT).show();
            // TODO: 跳转到对比分析页面
        });

        exportCard.setOnClickListener(v -> {
            Toast.makeText(getContext(), "导出光照报告", Toast.LENGTH_SHORT).show();
            // TODO: 生成并导出PDF/Excel报告
        });
    }

    private void startAnimations() {
        // 太阳光线旋转动画
        sunRotationAnimator = ObjectAnimator.ofFloat(raysIcon, "rotation", 0f, 360f);
        sunRotationAnimator.setDuration(20000);
        sunRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
        sunRotationAnimator.setInterpolator(new LinearInterpolator());
        sunRotationAnimator.start();

        // 光晕脉冲动画
        glowPulseAnimator = ObjectAnimator.ofFloat(glowEffect1, "alpha", 0.3f, 0.6f, 0.3f);
        glowPulseAnimator.setDuration(2000);
        glowPulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        glowPulseAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        glowPulseAnimator.start();

        // 为其他光晕添加类似动画
        ObjectAnimator glow2Animator = ObjectAnimator.ofFloat(glowEffect2, "alpha", 0.5f, 0.8f, 0.5f);
        glow2Animator.setDuration(2500);
        glow2Animator.setRepeatCount(ValueAnimator.INFINITE);
        glow2Animator.setInterpolator(new AccelerateDecelerateInterpolator());
        glow2Animator.start();

        ObjectAnimator glow3Animator = ObjectAnimator.ofFloat(glowEffect3, "alpha", 0.7f, 1.0f, 0.7f);
        glow3Animator.setDuration(3000);
        glow3Animator.setRepeatCount(ValueAnimator.INFINITE);
        glow3Animator.setInterpolator(new AccelerateDecelerateInterpolator());
        glow3Animator.start();
    }

    private void updateData() {
        // 模拟光照强度数据（可以从传感器获取）
        // 这里使用随机数模拟自然光照变化
        currentLuxValue = getLuxValueBasedOnTime();

        // 更新显示值
        DecimalFormat df = new DecimalFormat("#,###");
        currentValue.setText(df.format(currentLuxValue));

        // 更新状态
        updateStatus(currentLuxValue);

        // 更新描述文本
        updateDescription(currentLuxValue);

        // 更新时间
        updateLastUpdateTime();

        // 更新指示器位置
        updateIntensityIndicator(currentLuxValue);
    }

    private int getLuxValueBasedOnTime() {
        // 根据当前时间模拟真实的光照强度变化
        SimpleDateFormat sdf = new SimpleDateFormat("HH", Locale.getDefault());
        int hour = Integer.parseInt(sdf.format(new Date()));

        int baseLux;
        if (hour >= 6 && hour < 8) {
            // 日出时段：逐渐增强
            baseLux = 3000 + (hour - 6) * 3000;
        } else if (hour >= 8 && hour < 11) {
            // 上午：快速增强
            baseLux = 9000 + (hour - 8) * 2500;
        } else if (hour >= 11 && hour < 15) {
            // 正午：最强光照
            baseLux = 15000 + random.nextInt(5000);
        } else if (hour >= 15 && hour < 18) {
            // 下午：逐渐减弱
            baseLux = 15000 - (hour - 15) * 3000;
        } else if (hour >= 18 && hour < 20) {
            // 傍晚：快速减弱
            baseLux = 6000 - (hour - 18) * 2000;
        } else {
            // 夜晚：微弱光照
            baseLux = 100 + random.nextInt(400);
        }

        // 添加随机波动
        int fluctuation = random.nextInt(1000) - 500;
        return Math.max(0, baseLux + fluctuation);
    }

    private void updateStatus(int luxValue) {
        String status;
        int color;
        int bgRes;
        int iconRes;

        if (luxValue < 1000) {
            status = "光照不足";
            color = getResources().getColor(R.color.status_danger);
            bgRes = R.drawable.bg_status_tag_red;
            iconRes = R.drawable.ic_warning;
        } else if (luxValue < 5000) {
            status = "光照偏弱";
            color = getResources().getColor(R.color.status_warning);
            bgRes = R.drawable.bg_status_tag_orange;
            iconRes = R.drawable.ic_warning;
        } else if (luxValue < 10000) {
            status = "光照适中";
            color = getResources().getColor(R.color.status_info);
            bgRes = R.drawable.bg_status_tag_blue;
            iconRes = R.drawable.ic_check_circle;
        } else if (luxValue < 25000) {
            status = "光照充足";
            color = getResources().getColor(R.color.status_success);
            bgRes = R.drawable.bg_status_tag_green;
            iconRes = R.drawable.ic_check_circle;
        } else {
            status = "光照强烈";
            color = getResources().getColor(R.color.status_warning);
            bgRes = R.drawable.bg_status_tag_orange;
            iconRes = R.drawable.ic_sun;
        }

        statusText.setText(status);
        statusText.setTextColor(color);
        statusText.setBackgroundResource(bgRes);
        statusText.setCompoundDrawablesWithIntrinsicBounds(iconRes, 0, 0, 0);
    }

    private void updateDescription(int luxValue) {
        String description;

        if (luxValue < 1000) {
            description = "当前光照不足，建议开启补光灯。大多数植物在此光照条件下生长缓慢，可能出现徒长现象。" +
                    "适合耐阴植物如：蕨类、绿萝、吊兰等。";
        } else if (luxValue < 5000) {
            description = "当前光照偏弱，类似阴天环境。适合喜阴或半阴植物生长。" +
                    "建议延长光照时间或移至光线更充足的位置。";
        } else if (luxValue < 10000) {
            description = "当前光照适中，类似室内明亮环境。适合大部分室内观赏植物。" +
                    "可以种植大部分叶菜类蔬菜。";
        } else if (luxValue < 25000) {
            description = "当前光照强度充足，非常适合植物进行光合作用。大多数蔬菜和果树在此光照条件下生长良好。" +
                    "是理想的植物生长光照环境。";
        } else {
            description = "当前光照强烈，接近或达到直射阳光强度。注意观察植物是否出现晒伤现象。" +
                    "适合喜阳植物，但需要确保水分供应充足。";
        }

        descriptionText.setText(description);
    }

    private void updateIntensityIndicator(int luxValue) {
        // 根据光照值计算指示器位置（0-30000 Lux映射到0-100%）
        float percentage = Math.min(luxValue / 30000f, 1.0f);

        // 更新指示器位置（使用动画）
        if (intensityIndicator != null && intensityIndicator.getParent() != null) {
            View parent = (View) intensityIndicator.getParent();
            float targetMargin = parent.getWidth() * percentage;

            ValueAnimator animator = ValueAnimator.ofFloat(
                    intensityIndicator.getTranslationX(),
                    targetMargin - intensityIndicator.getWidth() / 2
            );
            animator.setDuration(500);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                float value = (float) animation.getAnimatedValue();
                intensityIndicator.setTranslationX(value);
            });
            animator.start();
        }
    }

    private void updateLastUpdateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        lastUpdateTime.setText("实时更新 " + currentTime);
    }

    private void refreshData() {
        Toast.makeText(getContext(), "正在刷新光照数据...", Toast.LENGTH_SHORT).show();

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

        // 增强太阳动画效果
        sunIcon.animate()
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(250)
                .withEndAction(() -> {
                    sunIcon.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(250)
                            .start();
                })
                .start();
    }

    private void startAutoRefresh() {
        // 每30秒自动刷新一次数据
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateData();
                handler.postDelayed(this, 30000); // 30秒
            }
        }, 30000);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
        if (sunRotationAnimator != null && !sunRotationAnimator.isRunning()) {
            sunRotationAnimator.start();
        }
        if (glowPulseAnimator != null && !glowPulseAnimator.isRunning()) {
            glowPulseAnimator.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sunRotationAnimator != null) {
            sunRotationAnimator.pause();
        }
        if (glowPulseAnimator != null) {
            glowPulseAnimator.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        handler.removeCallbacksAndMessages(null);
        if (sunRotationAnimator != null) {
            sunRotationAnimator.cancel();
        }
        if (glowPulseAnimator != null) {
            glowPulseAnimator.cancel();
        }
    }
}