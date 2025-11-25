package com.linjiu.recognize.layout.bottomNav.monitor.module;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

public class DataAnalysisFragment extends Fragment {

    private TextView detailTitle;
    private TextView currentValue;
    private TextView statusText;
    private TextView descriptionText;
    private CardView overviewCard;
    private CardView chartsCard;
    private CardView reportsCard;
    private CardView exportCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.data_analysis_module_fragment, container, false);

        // 初始化视图组件
        initViews(view);

        // 设置页面数据
        setupPageData();

        // 设置点击事件
        setupClickListeners();

        // 添加进入动画
        animateCardsEntrance();

        return view;
    }

    private void initViews(View view) {
        detailTitle = view.findViewById(R.id.detailTitle);
        currentValue = view.findViewById(R.id.currentValue);
        statusText = view.findViewById(R.id.statusText);
        descriptionText = view.findViewById(R.id.descriptionText);
        overviewCard = view.findViewById(R.id.overviewCard);
        chartsCard = view.findViewById(R.id.chartsCard);
        reportsCard = view.findViewById(R.id.reportsCard);
        exportCard = view.findViewById(R.id.exportCard);
    }

    private void setupPageData() {
        // 设置页面标题
        detailTitle.setText("数据分析中心");

        // 设置当前值
        currentValue.setText("分析完成");

        // 设置状态
        statusText.setText("● 就绪");

        // 设置描述文本
        descriptionText.setText("综合分析各项环境数据，生成可视化报告，帮助优化种植策略。");
    }

    private void setupClickListeners() {
        // 数据概览卡片
        overviewCard.setOnClickListener(v -> {
            animateCardClick(overviewCard);
            showToast("正在加载数据概览...");
            // TODO: 导航到数据概览页面
        });

        // 图表分析卡片
        chartsCard.setOnClickListener(v -> {
            animateCardClick(chartsCard);
            showToast("正在生成图表分析...");
            // TODO: 导航到图表分析页面
        });

        // 生成报告卡片
        reportsCard.setOnClickListener(v -> {
            animateCardClick(reportsCard);
            showToast("正在生成分析报告...");
            // TODO: 导航到报告生成页面
        });

        // 导出数据卡片
        exportCard.setOnClickListener(v -> {
            animateCardClick(exportCard);
            showToast("准备导出数据...");
            // TODO: 打开导出选项对话框
        });
    }

    /**
     * 卡片入场动画
     */
    private void animateCardsEntrance() {
        CardView[] cards = {overviewCard, chartsCard, reportsCard, exportCard};

        for (int i = 0; i < cards.length; i++) {
            CardView card = cards[i];
            card.setAlpha(0f);
            card.setTranslationY(100f);

            card.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(i * 100L)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    /**
     * 卡片点击动画效果
     */
    private void animateCardClick(CardView card) {
        // 缩放动画
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.95f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.95f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(card, "scaleX", 0.95f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(card, "scaleY", 0.95f, 1f);
        scaleUpX.setDuration(100);
        scaleUpY.setDuration(100);

        AnimatorSet scaleDown = new AnimatorSet();
        scaleDown.play(scaleDownX).with(scaleDownY);

        AnimatorSet scaleUp = new AnimatorSet();
        scaleUp.play(scaleUpX).with(scaleUpY);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleDown).before(scaleUp);
        animatorSet.start();
    }

    /**
     * 显示提示信息
     */
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 更新状态显示
     */
    public void updateStatus(String status, String value) {
        if (statusText != null) {
            statusText.setText("● " + status);
        }
        if (currentValue != null) {
            currentValue.setText(value);
        }
    }

    /**
     * 刷新数据统计
     */
    public void refreshStatistics(int dataPoints, int accuracy, String monitoring) {
        // TODO: 更新统计数据显示
        // 可以通过findViewById找到对应的TextView进行更新
    }
}