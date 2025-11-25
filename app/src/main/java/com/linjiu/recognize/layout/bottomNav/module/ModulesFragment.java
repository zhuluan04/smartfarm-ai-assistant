package com.linjiu.recognize.layout.bottomNav.module;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.linjiu.recognize.R;
import com.linjiu.recognize.layout.home.HomeActivity;
import com.linjiu.recognize.layout.program.MiniProgramContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * Optimized Modules Fragment with enhanced animations and UX
 */
public class ModulesFragment extends Fragment {

    private List<CardView> cardViews;
    private Handler animationHandler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_modules, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        animationHandler = new Handler(Looper.getMainLooper());
        initializeCardViews(view);
        setupClickListeners(view);
        animateCardsEntrance();
        setupHoverEffects();
    }

    /**
     * Initialize card views list
     */
    private void initializeCardViews(View view) {
        cardViews = new ArrayList<>();
        cardViews.add(view.findViewById(R.id.card_grow));
        cardViews.add(view.findViewById(R.id.card_foster));
        cardViews.add(view.findViewById(R.id.card_botany));
        cardViews.add(view.findViewById(R.id.card_history));
        cardViews.add(view.findViewById(R.id.card_analyze));
        cardViews.add(view.findViewById(R.id.card_wait));

        // Set initial state for animations
        for (CardView cardView : cardViews) {
            cardView.setAlpha(0f);
            cardView.setScaleX(0.85f);
            cardView.setScaleY(0.85f);
            cardView.setTranslationY(60f);
        }
    }

    /**
     * Setup click listeners for cards
     */
    private void setupClickListeners(View view) {
        setupCardClickListener(view.findViewById(R.id.card_grow), () -> {
            navigateToFragment(new PlantGrowthFragment(), "生长情况");
        });

        setupCardClickListener(view.findViewById(R.id.card_foster), () -> {
            navigateToFragment(new PlantFosterFragment(), "成长设置");
        });

        setupCardClickListener(view.findViewById(R.id.card_botany), () -> {
            navigateToFragment(new PlantBotanyFragment(), "植物库");
        });

        // 历史分析
        setupCardClickListener(view.findViewById(R.id.card_history), () -> {
            navigateToFragment(new PlantHistoryFragment(), "历史分析");
        });

        // 环境设置
        setupCardClickListener(view.findViewById(R.id.card_analyze), () -> {
            navigateToFragment(new PlantAnalyzeFragment(), "信息分析");
        });

        // 环境设置卡片
        setupCardClickListener(view.findViewById(R.id.environment), () -> {
            navigateToFragment(new PlantEnvironmentFragment(), "环境设置");
        });

        // 小程序部分
        setupCardClickListener(view.findViewById(R.id.card_tools), () -> {
            navigateToFragment(new MiniProgramContainer(), "小程序");
        });

        // 更多功能
        setupCardClickListener(view.findViewById(R.id.card_wait), () -> {
            showComingSoonToast("更多精彩功能正在开发中... 🚀");
        });
    }

    /**
     * Setup click listener with press animation
     */
    private void setupCardClickListener(CardView cardView, Runnable onClickAction) {
        if (cardView == null) return;

        cardView.setOnClickListener(v -> {
            animateCardPress(cardView, onClickAction);
        });
    }

    /**
     * Card press animation with refined timing
     */
    private void animateCardPress(CardView cardView, Runnable onClickAction) {
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(cardView, "scaleX", 1.0f, 0.92f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(cardView, "scaleY", 1.0f, 0.92f);
        ObjectAnimator elevationDown = ObjectAnimator.ofFloat(cardView, "cardElevation", 6f, 3f);

        AnimatorSet pressAnimator = new AnimatorSet();
        pressAnimator.playTogether(scaleDownX, scaleDownY, elevationDown);
        pressAnimator.setDuration(120);
        pressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(cardView, "scaleX", 0.92f, 1.05f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(cardView, "scaleY", 0.92f, 1.05f);
        ObjectAnimator elevationUp = ObjectAnimator.ofFloat(cardView, "cardElevation", 3f, 10f);

        AnimatorSet releaseAnimator = new AnimatorSet();
        releaseAnimator.playTogether(scaleUpX, scaleUpY, elevationUp);
        releaseAnimator.setDuration(180);
        releaseAnimator.setInterpolator(new OvershootInterpolator(2f));

        pressAnimator.start();
        animationHandler.postDelayed(() -> {
            releaseAnimator.start();
            animationHandler.postDelayed(onClickAction::run, 180);
        }, 120);
    }

    /**
     * Refined card entrance animation
     */
    private void animateCardsEntrance() {
        for (int i = 0; i < cardViews.size(); i++) {
            final CardView cardView = cardViews.get(i);
            final int delay = i * 120;

            animationHandler.postDelayed(() -> {
                if (cardView != null && isAdded()) {
                    ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(cardView, "alpha", 0f, 1f);
                    ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(cardView, "scaleX", 0.85f, 1f);
                    ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(cardView, "scaleY", 0.85f, 1f);
                    ObjectAnimator translationYAnimator = ObjectAnimator.ofFloat(cardView, "translationY", 60f, 0f);

                    AnimatorSet enterAnimator = new AnimatorSet();
                    enterAnimator.playTogether(alphaAnimator, scaleXAnimator, scaleYAnimator, translationYAnimator);
                    enterAnimator.setDuration(500);
                    enterAnimator.setInterpolator(new FastOutSlowInInterpolator());
                    enterAnimator.start();
                }
            }, delay);
        }
    }

    /**
     * Setup hover effects for cards
     */
    private void setupHoverEffects() {
        for (CardView cardView : cardViews) {
            if (cardView != null) {
                cardView.setOnTouchListener((v, event) -> {
                    switch (event.getAction()) {
                        case android.view.MotionEvent.ACTION_DOWN:
                            ObjectAnimator elevationAnimator = ObjectAnimator.ofFloat(cardView, "cardElevation", 6f, 12f);
                            elevationAnimator.setDuration(150);
                            elevationAnimator.start();
                            break;

                        case android.view.MotionEvent.ACTION_UP:
                        case android.view.MotionEvent.ACTION_CANCEL:
                            ObjectAnimator restoreElevationAnimator = ObjectAnimator.ofFloat(cardView, "cardElevation", 12f, 6f);
                            restoreElevationAnimator.setDuration(150);
                            restoreElevationAnimator.start();
                            break;
                    }
                    return false;
                });
            }
        }
    }

    /**
     * Navigate to specified Fragment
     */
    private void navigateToFragment(Fragment fragment, String moduleName) {
        if (getActivity() instanceof HomeActivity) {
            showLoadingToast("正在进入" + moduleName + "...");
            animationHandler.postDelayed(() -> {
                ((HomeActivity) getActivity()).navigateToFragment(fragment);
            }, 400);
        }
    }

    /**
     * Show coming soon toast
     */
    private void showComingSoonToast(String message) {
        if (getContext() != null) {
            Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Show loading toast
     */
    private void showLoadingToast(String message) {
        if (getContext() != null) {
            Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (animationHandler != null) {
            animationHandler.removeCallbacksAndMessages(null);
            animationHandler = null;
        }
        if (cardViews != null) {
            cardViews.clear();
            cardViews = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && getView() != null && cardViews != null) {
            boolean needsAnimation = true;
            for (CardView cardView : cardViews) {
                if (cardView.getAlpha() == 1f) {
                    needsAnimation = false;
                    break;
                }
            }

            if (needsAnimation) {
                animationHandler.postDelayed(this::animateCardsEntrance, 300);
            }
        }
    }
}