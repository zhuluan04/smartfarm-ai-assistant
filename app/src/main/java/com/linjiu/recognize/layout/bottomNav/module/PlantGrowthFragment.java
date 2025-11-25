package com.linjiu.recognize.layout.bottomNav.module;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;
import com.linjiu.recognize.layout.home.HomeActivity;

public class PlantGrowthFragment extends Fragment {

    private ImageView plantImage;
    private TextView plantName, plantType, plantHeight, plantLeaves, soilMoisture, temperature, growthPercent;
    private ProgressBar growthProgress;
    private Button historyButton;
    private CardView plantCard, infoCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modules_growth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 绑定控件
        plantImage = view.findViewById(R.id.plantImage);
        plantName = view.findViewById(R.id.plantName);
        plantType = view.findViewById(R.id.plantType);
        plantHeight = view.findViewById(R.id.plantHeight);
        plantLeaves = view.findViewById(R.id.plantLeaves);
        soilMoisture = view.findViewById(R.id.soilMoisture);
        temperature = view.findViewById(R.id.temperature);
        growthProgress = view.findViewById(R.id.growthProgress);
        growthPercent = view.findViewById(R.id.growthPercent);
        historyButton = view.findViewById(R.id.historyButton);
        plantCard = view.findViewById(R.id.plantCard);
        infoCard = view.findViewById(R.id.infoCard);

        // 设置数据
        setupPlantData();

        // 动画效果
        animateGrowthProgress();
        animateCards();

        // 跳转历史页面
        if (historyButton != null) {
            historyButton.setOnClickListener(v -> {
                if (getActivity() instanceof HomeActivity) {
                    ((HomeActivity) getActivity()).navigateToFragment(new PlantHistoryFragment());
                }
            });
        }
    }

    private void setupPlantData() {
        // 植物信息
        if (plantImage != null) plantImage.setImageResource(R.drawable.ic_plant);
        if (plantName != null) plantName.setText("🌿 绿萝");
        if (plantType != null) plantType.setText("攀缘植物 | 常绿观叶");
        if (plantHeight != null) plantHeight.setText("📏 高度：12.5 cm");
        if (plantLeaves != null) plantLeaves.setText("🍃 叶片数：8 片");
        if (soilMoisture != null) soilMoisture.setText("💧 土壤湿度：65%");
        if (temperature != null) temperature.setText("🌡️ 温度：23℃");

        // 生长进度
        if (growthProgress != null) {
            growthProgress.setProgress(40);
            if (growthPercent != null) growthPercent.setText("40%");
        }
    }

    private void animateGrowthProgress() {
        if (growthProgress != null) {
            // 重置进度
            growthProgress.setProgress(0);

            // 动画进度
            ObjectAnimator animator = ObjectAnimator.ofInt(growthProgress, "progress", 0, 40);
            animator.setDuration(1500);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();

            // 同步百分比文本动画
            animateTextProgress(0, 40, 1500);
        }
    }

    private void animateTextProgress(int start, int end, long duration) {
        if (growthPercent != null) {
            ValueAnimator animator = ValueAnimator.ofInt(start, end);
            animator.setDuration(duration);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.addUpdateListener(animation -> {
                int value = (int) animation.getAnimatedValue();
                growthPercent.setText(value + "%");
            });
            animator.start();
        }
    }

    private void animateCards() {
        // 植物卡片动画
        if (plantCard != null) {
            plantCard.setTranslationY(50);
            plantCard.setAlpha(0f);
            plantCard.animate()
                    .translationY(0)
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(200)
                    .start();
        }

        // 信息卡片动画
        if (infoCard != null) {
            infoCard.setTranslationY(50);
            infoCard.setAlpha(0f);
            infoCard.animate()
                    .translationY(0)
                    .alpha(1f)
                    .setDuration(800)
                    .setStartDelay(400)
                    .start();
        }
    }
}