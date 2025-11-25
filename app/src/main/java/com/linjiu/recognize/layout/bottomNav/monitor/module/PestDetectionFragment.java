package com.linjiu.recognize.layout.bottomNav.monitor.module;

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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

public class PestDetectionFragment extends Fragment {
    private TextView detailTitle;
    private TextView currentValue;
    private TextView statusText;
    private TextView descriptionText;
    private TextView scanCount;
    private TextView detectionRate;
    private TextView monitoringStatus;
    private TextView lastScanTime;

    private ImageView statusIcon;
    private View pulseRing;
    private View statusIndicator;

    private CardView historyCard;
    private CardView cameraCard;
    private CardView reportCard;
    private CardView preventionCard;

    private Handler handler;
    private Runnable pulseRunnable;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pest_detection_module_fragment, container, false);

        // 初始化Handler
        handler = new Handler();

        // 初始化视图组件
        initViews(view);

        // 设置页面数据
        setupPageData();

        // 设置点击事件
        setupClickListeners();

        // 添加动画效果
        setupAnimations(view);

        // 启动脉动动画
        startPulseAnimation();

        return view;
    }

    private void initViews(View view) {
        detailTitle = view.findViewById(R.id.detailTitle);
        currentValue = view.findViewById(R.id.currentValue);
        statusText = view.findViewById(R.id.statusText);
        descriptionText = view.findViewById(R.id.descriptionText);
        scanCount = view.findViewById(R.id.scanCount);
        detectionRate = view.findViewById(R.id.detectionRate);
        monitoringStatus = view.findViewById(R.id.monitoringStatus);
        lastScanTime = view.findViewById(R.id.lastScanTime);

        statusIcon = view.findViewById(R.id.statusIcon);
        pulseRing = view.findViewById(R.id.pulseRing);
        statusIndicator = view.findViewById(R.id.statusIndicator);

        historyCard = view.findViewById(R.id.historyCard);
        cameraCard = view.findViewById(R.id.cameraCard);
        reportCard = view.findViewById(R.id.reportCard);
        preventionCard = view.findViewById(R.id.preventionCard);
    }

    private void setupPageData() {
        // 设置基础信息
        detailTitle.setText("病虫害监测");
        currentValue.setText("正常");
        statusText.setText("安全");
        descriptionText.setText("通过AI图像识别技术实时监测植物健康状况，精准识别病虫害风险，守护植物茁壮成长。");

        // 模拟今日扫描次数（带动画）
        animateCounter(scanCount, 0, 24, 1500);

        // 设置检出率
        detectionRate.setText("0%");

        // 设置监测状态
        monitoringStatus.setText("运行中");

        // 设置最后扫描时间
        updateLastScanTime();
    }

    private void setupClickListeners() {
        // 拍照检测 - 主要功能
        cameraCard.setOnClickListener(v -> {
            addCardClickAnimation(cameraCard);
            showToast("正在启动相机...");
            // TODO: 打开相机进行拍照检测
        });

        // 历史记录
        historyCard.setOnClickListener(v -> {
            addCardClickAnimation(historyCard);
            showToast("正在加载历史记录...");
            // TODO: 跳转到历史记录页面
        });

        // 生成报告
        reportCard.setOnClickListener(v -> {
            addCardClickAnimation(reportCard);
            showToast("正在生成检测报告...");
            // TODO: 生成并下载报告
        });

        // 预防建议
        preventionCard.setOnClickListener(v -> {
            addCardClickAnimation(preventionCard);
            showToast("正在加载预防建议...");
            // TODO: 显示AI预防建议
        });
    }

    /**
     * 启动脉动动画 - 模拟实时监测效果
     */
    private void startPulseAnimation() {
        if (pulseRing != null) {
            ObjectAnimator scaleX = ObjectAnimator.ofFloat(pulseRing, "scaleX", 1f, 1.2f, 1f);
            ObjectAnimator scaleY = ObjectAnimator.ofFloat(pulseRing, "scaleY", 1f, 1.2f, 1f);
            ObjectAnimator alpha = ObjectAnimator.ofFloat(pulseRing, "alpha", 0.3f, 0.1f, 0.3f);

            scaleX.setDuration(2000);
            scaleY.setDuration(2000);
            alpha.setDuration(2000);

            scaleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleY.setRepeatCount(ValueAnimator.INFINITE);
            alpha.setRepeatCount(ValueAnimator.INFINITE);

            scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
            scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
            alpha.setInterpolator(new AccelerateDecelerateInterpolator());

            scaleX.start();
            scaleY.start();
            alpha.start();
        }

        // 状态指示器闪烁
        startStatusIndicatorBlink();
    }

    /**
     * 状态指示器闪烁动画
     */
    private void startStatusIndicatorBlink() {
        if (statusIndicator != null) {
            ObjectAnimator blink = ObjectAnimator.ofFloat(statusIndicator, "alpha", 1f, 0.3f, 1f);
            blink.setDuration(1500);
            blink.setRepeatCount(ValueAnimator.INFINITE);
            blink.setInterpolator(new LinearInterpolator());
            blink.start();
        }
    }

    /**
     * 数字计数动画
     */
    private void animateCounter(TextView textView, int from, int to, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(from, to);
        animator.setDuration(duration);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            int value = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(value));
        });
        animator.start();
    }

    /**
     * 卡片点击动画
     */
    private void addCardClickAnimation(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .start();
                })
                .start();
    }

    /**
     * 入场动画
     */
    private void setupAnimations(View rootView) {
        // 标题淡入
        detailTitle.setAlpha(0f);
        detailTitle.animate()
                .alpha(1f)
                .setDuration(600)
                .start();

        // 监测圆缩放动画
        View monitorCircle = (View) rootView.findViewById(R.id.currentValue).getParent();
        if (monitorCircle instanceof View) {
            monitorCircle.setScaleX(0.8f);
            monitorCircle.setScaleY(0.8f);
            monitorCircle.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(800)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        // 卡片依次淡入
        animateCardWithDelay(cameraCard, 100);
        animateCardWithDelay(historyCard, 200);
        animateCardWithDelay(reportCard, 300);
        animateCardWithDelay(preventionCard, 400);
    }

    /**
     * 卡片延迟动画
     */
    private void animateCardWithDelay(View card, long delay) {
        card.setAlpha(0f);
        card.setTranslationY(50f);
        card.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(delay)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    /**
     * 更新最后扫描时间
     */
    private void updateLastScanTime() {
        // 模拟显示最后扫描时间
        lastScanTime.setText("30分钟前");
    }

    /**
     * 根据检测状态更新UI
     * @param isPestDetected 是否检测到病虫害
     */
    private void updateDetectionStatus(boolean isPestDetected) {
        if (getContext() == null) return;

        if (isPestDetected) {
            // 检测到病虫害 - 警告状态
            currentValue.setText("警告");
            currentValue.setTextColor(ContextCompat.getColor(getContext(), R.color.status_warning));
            statusText.setText("发现异常");
            statusText.setTextColor(ContextCompat.getColor(getContext(), R.color.status_warning));
            statusIcon.setImageResource(R.drawable.ic_warning);
            statusIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.status_warning));
            detectionRate.setText("12%");
            detectionRate.setTextColor(ContextCompat.getColor(getContext(), R.color.status_warning));
        } else {
            // 未检测到病虫害 - 安全状态
            currentValue.setText("正常");
            currentValue.setTextColor(ContextCompat.getColor(getContext(), R.color.status_safe));
            statusText.setText("安全");
            statusText.setTextColor(ContextCompat.getColor(getContext(), R.color.status_safe));
            statusIcon.setImageResource(R.drawable.ic_shield_check);
            statusIcon.setColorFilter(ContextCompat.getColor(getContext(), R.color.status_safe));
            detectionRate.setText("0%");
            detectionRate.setTextColor(ContextCompat.getColor(getContext(), R.color.status_safe));
        }
    }

    /**
     * 显示Toast提示
     */
    private void showToast(String message) {
        if (getContext() != null) {
            android.widget.Toast.makeText(getContext(), message, android.widget.Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 恢复脉动动画
        startPulseAnimation();
        // 更新时间
        updateLastScanTime();
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止所有动画
        if (pulseRing != null) {
            pulseRing.clearAnimation();
        }
        if (statusIndicator != null) {
            statusIndicator.clearAnimation();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清理Handler
        if (handler != null && pulseRunnable != null) {
            handler.removeCallbacks(pulseRunnable);
        }
    }
}