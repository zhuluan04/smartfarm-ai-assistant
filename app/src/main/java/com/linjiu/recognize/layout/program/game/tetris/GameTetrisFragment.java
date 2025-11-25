package com.linjiu.recognize.layout.program.game.tetris;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

// 俄罗斯方块游戏 Fragment
public class GameTetrisFragment extends Fragment {

    private TetrisView tetrisView;
    private TextView tvScore, tvLevel, tvLines;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game_tetris, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        tetrisView = view.findViewById(R.id.tetrisView);

        tvScore = view.findViewById(R.id.tvScore);
        tvLevel = view.findViewById(R.id.tvLevel);
        tvLines = view.findViewById(R.id.tvLines);

        // 设置返回
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        // 绑定控制按钮
        view.findViewById(R.id.btnLeft).setOnClickListener(v -> {
            if (tetrisView != null) tetrisView.controlLeft();
        });
        view.findViewById(R.id.btnRight).setOnClickListener(v -> {
            if (tetrisView != null) tetrisView.controlRight();
        });
        view.findViewById(R.id.btnRotate).setOnClickListener(v -> {
            if (tetrisView != null) tetrisView.controlRotate();
        });
        view.findViewById(R.id.btnDown).setOnClickListener(v -> {
            if (tetrisView != null) tetrisView.controlDown();
        });

        // 暂停按钮
        Button btnPause = view.findViewById(R.id.btnPause);
        if (btnPause != null) {
            btnPause.setOnClickListener(v -> {
                if (tetrisView != null) {
                    tetrisView.togglePause();
                    btnPause.setText(tetrisView.isPaused() ? "▶️ 继续" : "⏸️ 暂停");
                }
            });
        }

        // 设置游戏信息回调
        if (tetrisView != null) {
            tetrisView.setGameInfoListener(new TetrisView.OnGameInfoUpdateListener() {
                @Override
                public void onGameInfoUpdated(int score, int lines, int level) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if (tvScore != null) tvScore.setText("分数: " + score);
                            if (tvLines != null) tvLines.setText("消除: " + lines);
                            if (tvLevel != null) tvLevel.setText("等级: " + level);
                        });
                    }
                }

                @Override
                public void onGameOver() {
                    if (getActivity() != null && !getActivity().isFinishing()) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(getContext(), "游戏结束！点击游戏区域重玩", Toast.LENGTH_LONG).show();
                            // 设置点击重启（只设置一次，避免重复绑定）
                            if (tetrisView != null) {
                                tetrisView.setOnClickListener(v -> {
                                    if (tetrisView.isGameOver()) {
                                        tetrisView.restartGame();
                                    }
                                });
                            }
                        });
                    }
                }
            });

            // ✅ 关键修复：初始化后启动游戏（或让用户点击开始）
            // 方案一：自动开始游戏（推荐新手体验）
            tetrisView.restartGame();

            // 方案二：点击开始（更安全，避免生命周期冲突）
            /*
            tetrisView.setOnClickListener(v -> {
                if (tetrisView.isGameOver()) {
                    tetrisView.restartGame();
                }
            });
            tetrisView.setClickable(true);
            */
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (tetrisView != null && !tetrisView.isGameOver()) {
            tetrisView.pauseGame();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (tetrisView != null && tetrisView.isPaused() && !tetrisView.isGameOver()) {
            tetrisView.resumeGame();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清理引用，避免内存泄漏
        if (tetrisView != null) {
            tetrisView.setGameInfoListener(null);
            tetrisView.setOnClickListener(null);
        }
        tetrisView = null;
        tvScore = null;
        tvLevel = null;
        tvLines = null;
    }
}