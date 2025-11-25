package com.linjiu.recognize.layout.bottomNav.monitor.devices;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// 土壤养分详情页面
public class NutrientDetailFragment extends Fragment {

    private TextView nValueText, pValueText, kValueText;
    private TextView nLevelText, pLevelText, kLevelText;
    private TextView statusText, descriptionText, lastUpdateTime;
    private View statusIndicator;
    private ImageView backButton, refreshButton;
    private CardView historyCard, settingsCard, trendCard, alertCard, exportCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.devices_nutrient_detail_fragment, container, false);

        initViews(view);
        setupClickListeners();
        updateData();

        return view;
    }

    private void initViews(View view) {
        // 顶部控件
        backButton = view.findViewById(R.id.backButton);
        refreshButton = view.findViewById(R.id.refreshButton);

        // 状态指示器
        statusIndicator = view.findViewById(R.id.statusIndicator);
        lastUpdateTime = view.findViewById(R.id.lastUpdateTime);

        // NPK数值
        nValueText = view.findViewById(R.id.nValue);
        pValueText = view.findViewById(R.id.pValue);
        kValueText = view.findViewById(R.id.kValue);

        // NPK等级
        nLevelText = view.findViewById(R.id.nLevel);
        pLevelText = view.findViewById(R.id.pLevel);
        kLevelText = view.findViewById(R.id.kLevel);

        // 状态和描述
        statusText = view.findViewById(R.id.statusText);
        descriptionText = view.findViewById(R.id.descriptionText);

        // 功能卡片
        historyCard = view.findViewById(R.id.historyCard);
        settingsCard = view.findViewById(R.id.settingsCard);
        trendCard = view.findViewById(R.id.trendCard);
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
            // 跳转到历史数据页面
            Toast.makeText(getContext(), "查看历史数据", Toast.LENGTH_SHORT).show();
            // TODO: 实现历史数据图表页面
        });

        settingsCard.setOnClickListener(v -> {
            // 跳转到传感器设置页面
            Toast.makeText(getContext(), "传感器设置", Toast.LENGTH_SHORT).show();
            // TODO: 实现传感器设置页面
        });

        trendCard.setOnClickListener(v -> {
            // 跳转到趋势分析页面
            Toast.makeText(getContext(), "查看变化趋势", Toast.LENGTH_SHORT).show();
            // TODO: 实现趋势分析页面
        });

        alertCard.setOnClickListener(v -> {
            // 跳转到预警设置页面
            Toast.makeText(getContext(), "预警设置", Toast.LENGTH_SHORT).show();
            // TODO: 实现预警设置页面
        });

        exportCard.setOnClickListener(v -> {
            // 导出报告功能
            Toast.makeText(getContext(), "导出报告", Toast.LENGTH_SHORT).show();
            // TODO: 实现报告导出功能
        });
    }

    private void updateData() {
        // 模拟从传感器获取的数据
        int nValue = 68;  // 氮含量 mg/kg
        int pValue = 32;  // 磷含量 mg/kg
        int kValue = 145; // 钾含量 mg/kg

        // 更新NPK数值
        nValueText.setText(String.valueOf(nValue));
        pValueText.setText(String.valueOf(pValue));
        kValueText.setText(String.valueOf(kValue));

        // 更新NPK等级
        updateNutrientLevel(nValue, nLevelText, "N");
        updateNutrientLevel(pValue, pLevelText, "P");
        updateNutrientLevel(kValue, kLevelText, "K");

        // 更新综合状态
        updateOverallStatus(nValue, pValue, kValue);

        // 更新时间
        updateLastUpdateTime();

        // 更新建议
        updateRecommendation(nValue, pValue, kValue);
    }

    private void updateNutrientLevel(int value, TextView levelText, String type) {
        String level;
        int color;
        int bgRes;

        if (type.equals("N")) {
            if (value < 50) {
                level = "偏低";
                color = getResources().getColor(R.color.status_danger);
                bgRes = R.drawable.bg_status_tag_red;
            } else if (value < 80) {
                level = "中等";
                color = getResources().getColor(R.color.status_purple);
                bgRes = R.drawable.bg_status_tag_purple;
            } else {
                level = "充足";
                color = getResources().getColor(R.color.status_success);
                bgRes = R.drawable.bg_status_tag_green;
            }
        } else if (type.equals("P")) {
            if (value < 40) {
                level = "偏低";
                color = getResources().getColor(R.color.status_danger);
                bgRes = R.drawable.bg_status_tag_red;
            } else if (value < 70) {
                level = "中等";
                color = getResources().getColor(R.color.status_purple);
                bgRes = R.drawable.bg_status_tag_purple;
            } else {
                level = "充足";
                color = getResources().getColor(R.color.status_success);
                bgRes = R.drawable.bg_status_tag_green;
            }
        } else { // K
            if (value < 80) {
                level = "偏低";
                color = getResources().getColor(R.color.status_danger);
                bgRes = R.drawable.bg_status_tag_red;
            } else if (value < 120) {
                level = "中等";
                color = getResources().getColor(R.color.status_purple);
                bgRes = R.drawable.bg_status_tag_purple;
            } else {
                level = "充足";
                color = getResources().getColor(R.color.status_success);
                bgRes = R.drawable.bg_status_tag_green;
            }
        }

        levelText.setText(level);
        levelText.setTextColor(color);
        levelText.setBackgroundResource(bgRes);
    }

    private void updateOverallStatus(int nValue, int pValue, int kValue) {
        boolean nOk = nValue >= 50 && nValue <= 100;
        boolean pOk = pValue >= 40 && pValue <= 80;
        boolean kOk = kValue >= 80 && kValue <= 150;

        if (nOk && pOk && kOk) {
            statusText.setText("均衡");
            statusText.setTextColor(getResources().getColor(R.color.status_success));
            statusText.setBackgroundResource(R.drawable.bg_status_tag_green);
        } else if (!nOk || !pOk || !kOk) {
            statusText.setText("不平衡");
            statusText.setTextColor(getResources().getColor(R.color.status_warning));
            statusText.setBackgroundResource(R.drawable.bg_status_tag_orange);
        } else {
            statusText.setText("异常");
            statusText.setTextColor(getResources().getColor(R.color.status_danger));
            statusText.setBackgroundResource(R.drawable.bg_status_tag_red);
        }
    }

    private void updateRecommendation(int nValue, int pValue, int kValue) {
        StringBuilder recommendation = new StringBuilder();

        if (pValue < 40) {
            recommendation.append("检测到磷元素含量偏低，建议施用磷肥补充。");
        }

        if (nValue < 50) {
            if (recommendation.length() > 0) recommendation.append(" ");
            recommendation.append("氮元素含量偏低，需要补充氮肥。");
        } else if (nValue < 80) {
            if (recommendation.length() > 0) recommendation.append(" ");
            recommendation.append("氮元素处于中等水平。");
        } else {
            if (recommendation.length() > 0) recommendation.append(" ");
            recommendation.append("氮元素充足。");
        }

        if (kValue >= 120) {
            if (recommendation.length() > 0) recommendation.append(" ");
            recommendation.append("钾元素充足。");
        } else if (kValue < 80) {
            if (recommendation.length() > 0) recommendation.append(" ");
            recommendation.append("钾元素偏低，建议补充钾肥。");
        }

        if (recommendation.length() > 0) {
            recommendation.append(" 整体养分");
            if (nValue >= 50 && pValue >= 40 && kValue >= 80) {
                recommendation.append("均衡。");
            } else {
                recommendation.append("不够均衡。");
            }
        }

        descriptionText.setText(recommendation.toString());
    }

    private void updateLastUpdateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        lastUpdateTime.setText("更新于 " + currentTime);
    }

    private void refreshData() {
        // 模拟刷新数据
        Toast.makeText(getContext(), "正在刷新数据...", Toast.LENGTH_SHORT).show();

        // 添加刷新动画
        refreshButton.animate()
                .rotation(360f)
                .setDuration(500)
                .withEndAction(() -> {
                    refreshButton.setRotation(0);
                    updateData();
                    Toast.makeText(getContext(), "数据已更新", Toast.LENGTH_SHORT).show();
                })
                .start();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 页面显示时刷新数据
        updateData();
    }
}