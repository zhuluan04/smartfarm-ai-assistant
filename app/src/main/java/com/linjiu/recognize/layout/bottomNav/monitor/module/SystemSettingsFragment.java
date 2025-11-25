package com.linjiu.recognize.layout.bottomNav.monitor.module;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
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

public class SystemSettingsFragment extends Fragment {

    private TextView detailTitle;
    private TextView currentValue;
    private TextView statusText;
    private TextView descriptionText;
    private CardView themeCard;
    private CardView notificationsCard;
    private CardView dataSyncCard;
    private CardView languageCard;
    private CardView backupCard;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.system_settings_module_fragment, container, false);

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
        themeCard = view.findViewById(R.id.themeCard);
        notificationsCard = view.findViewById(R.id.notificationsCard);
        dataSyncCard = view.findViewById(R.id.dataSyncCard);
        languageCard = view.findViewById(R.id.languageCard);
        backupCard = view.findViewById(R.id.backupCard);
    }

    private void setupPageData() {
        // 设置页面标题
        detailTitle.setText("系统设置");

        // 设置当前值
        currentValue.setText("个性化配置");

        // 设置状态
        statusText.setText("● 就绪");

        // 设置描述文本
        descriptionText.setText("自定义系统配置，个性化设置，优化使用体验。");
    }

    private void setupClickListeners() {
        // 主题设置
        themeCard.setOnClickListener(v -> {
            animateCardClick(themeCard);
            showThemeDialog();
        });

        // 通知设置
        notificationsCard.setOnClickListener(v -> {
            animateCardClick(notificationsCard);
            showToast("正在打开通知设置...");
            // TODO: 导航到通知设置页面
        });

        // 数据同步
        dataSyncCard.setOnClickListener(v -> {
            animateCardClick(dataSyncCard);
            showSyncDialog();
        });

        // 语言设置
        languageCard.setOnClickListener(v -> {
            animateCardClick(languageCard);
            showLanguageDialog();
        });

        // 备份恢复
        backupCard.setOnClickListener(v -> {
            animateCardClick(backupCard);
            showBackupDialog();
        });
    }

    /**
     * 卡片入场动画
     */
    private void animateCardsEntrance() {
        CardView[] cards = {themeCard, notificationsCard, languageCard, dataSyncCard, backupCard};

        for (int i = 0; i < cards.length; i++) {
            CardView card = cards[i];
            card.setAlpha(0f);
            card.setTranslationX(-100f);

            card.animate()
                    .alpha(1f)
                    .translationX(0f)
                    .setDuration(400)
                    .setStartDelay(i * 80L)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    /**
     * 卡片点击动画效果
     */
    private void animateCardClick(CardView card) {
        // 缩放动画
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 0.96f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 0.96f);
        scaleDownX.setDuration(100);
        scaleDownY.setDuration(100);

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(card, "scaleX", 0.96f, 1f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(card, "scaleY", 0.96f, 1f);
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
     * 显示主题选择对话框
     */
    private void showThemeDialog() {
        if (getContext() == null) return;

        String[] themes = {"浅色模式", "深色模式", "跟随系统"};

        new AlertDialog.Builder(getContext())
                .setTitle("选择主题")
                .setItems(themes, (dialog, which) -> {
                    String selectedTheme = themes[which];
                    showToast("已切换到: " + selectedTheme);
                    // TODO: 实际切换主题逻辑
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示同步选项对话框
     */
    private void showSyncDialog() {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("数据同步")
                .setMessage("是否立即同步数据到云端？")
                .setPositiveButton("立即同步", (dialog, which) -> {
                    showToast("正在同步数据...");
                    // TODO: 执行同步操作
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示语言选择对话框
     */
    private void showLanguageDialog() {
        if (getContext() == null) return;

        String[] languages = {"简体中文", "English", "日本語", "한국어"};

        new AlertDialog.Builder(getContext())
                .setTitle("选择语言")
                .setSingleChoiceItems(languages, 0, null)
                .setPositiveButton("确定", (dialog, which) -> {
                    showToast("语言设置已保存");
                    // TODO: 实际切换语言逻辑
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 显示备份恢复对话框
     */
    private void showBackupDialog() {
        if (getContext() == null) return;

        String[] options = {"立即备份", "恢复数据", "查看备份记录"};

        new AlertDialog.Builder(getContext())
                .setTitle("备份与恢复")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            showToast("正在备份数据...");
                            // TODO: 执行备份操作
                            break;
                        case 1:
                            showToast("准备恢复数据...");
                            // TODO: 执行恢复操作
                            break;
                        case 2:
                            showToast("查看备份记录");
                            // TODO: 显示备份记录
                            break;
                    }
                })
                .setNegativeButton("取消", null)
                .show();
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
     * 更新系统状态
     */
    public void updateSystemStatus(String status, String description) {
        if (statusText != null) {
            statusText.setText("● " + status);
        }
        if (descriptionText != null) {
            descriptionText.setText(description);
        }
    }

    /**
     * 更新同步状态
     */
    public void updateSyncStatus(boolean enabled) {
        // TODO: 更新数据同步卡片的状态显示
        showToast(enabled ? "数据同步已开启" : "数据同步已关闭");
    }
}