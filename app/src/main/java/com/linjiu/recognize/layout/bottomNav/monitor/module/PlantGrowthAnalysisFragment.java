package com.linjiu.recognize.layout.bottomNav.monitor.module;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PlantGrowthAnalysisFragment extends Fragment {
    private TextView detailTitle;
    private TextView currentValue;
    private TextView statusText;
    private TextView descriptionText;
    private TextView healthScore;
    private TextView growthSpeed;
    private TextView lastUpdateTime;
    private RatingBar healthRating;

    private CardView growthChartCard;
    private CardView nutritionCard;
    private CardView recommendationCard;
    private CardView historyCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.plant_growth_module_fragment, container, false);

        // 初始化视图组件
        initViews(view);

        // 设置页面数据
        setupPageData();

        // 设置点击事件
        setupClickListeners();

        // 添加入场动画
        setupAnimations(view);

        return view;
    }

    private void initViews(View view) {
        detailTitle = view.findViewById(R.id.detailTitle);
        currentValue = view.findViewById(R.id.currentValue);
        statusText = view.findViewById(R.id.statusText);
        descriptionText = view.findViewById(R.id.descriptionText);
        healthScore = view.findViewById(R.id.healthScore);
        growthSpeed = view.findViewById(R.id.growthSpeed);
        lastUpdateTime = view.findViewById(R.id.lastUpdateTime);
        healthRating = view.findViewById(R.id.healthRating);

        growthChartCard = view.findViewById(R.id.growthChartCard);
        nutritionCard = view.findViewById(R.id.nutritionCard);
        recommendationCard = view.findViewById(R.id.recommendationCard);
        historyCard = view.findViewById(R.id.historyCard);
    }

    private void setupPageData() {
        // 设置基础信息
        detailTitle.setText("植物生长分析");
        currentValue.setText("健康");
        statusText.setText("良好");
        descriptionText.setText("综合分析植物生长状态，提供科学的养护建议和生长趋势预测。AI智能监测系统24小时守护您的植物健康。");

        // 设置健康评分（带动画）
        animateScore(healthScore, 0, 92, 1500);

        // 设置生长速度
        growthSpeed.setText("优秀");

        // 设置评分星级
        healthRating.setRating(4.5f);

        // 设置最后更新时间
        updateLastUpdateTime();
    }

    private void setupClickListeners() {
        // 生长趋势图点击
        growthChartCard.setOnClickListener(v -> {
            addCardClickAnimation(growthChartCard);
            // TODO: 跳转到生长趋势图页面
            showToast("正在加载生长趋势图...");
        });

        // 营养分析点击
        nutritionCard.setOnClickListener(v -> {
            addCardClickAnimation(nutritionCard);
            // TODO: 跳转到营养分析页面
            showToast("正在加载营养分析...");
        });

        // 养护建议点击
        recommendationCard.setOnClickListener(v -> {
            addCardClickAnimation(recommendationCard);
            // TODO: 跳转到养护建议页面
            showToast("正在加载养护建议...");
        });

        // 历史记录点击
        historyCard.setOnClickListener(v -> {
            addCardClickAnimation(historyCard);
            // TODO: 跳转到历史记录页面
            showToast("正在加载历史记录...");
        });
    }

    /**
     * 数字滚动动画
     */
    private void animateScore(TextView textView, int from, int to, int duration) {
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
        // 缩放动画
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
        // 标题淡入动画
        detailTitle.setAlpha(0f);
        detailTitle.animate()
                .alpha(1f)
                .setDuration(600)
                .start();

        // 状态圈缩放动画
        View statusCircle = (View) rootView.findViewById(R.id.currentValue).getParent();
        if (statusCircle instanceof View) {
            statusCircle.setScaleX(0.8f);
            statusCircle.setScaleY(0.8f);
            statusCircle.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(800)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        // 卡片依次淡入
        animateCardWithDelay(growthChartCard, 200);
        animateCardWithDelay(nutritionCard, 300);
        animateCardWithDelay(recommendationCard, 400);
        animateCardWithDelay(historyCard, 500);
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
     * 更新最后更新时间
     */
    private void updateLastUpdateTime() {
        // 实际应该从服务器获取，这里模拟显示
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        lastUpdateTime.setText("2小时前");
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
        // 页面恢复时刷新数据
        updateLastUpdateTime();
    }
}