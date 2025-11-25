package com.linjiu.recognize.layout.program.game.snake;

import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

public class GameSnakeFragment extends Fragment {

    private SnakeView snakeView;

    private JoystickView joystickView;

    private TextView tvScore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game_snake, container, false);

        snakeView = view.findViewById(R.id.snakeView);
        joystickView = view.findViewById(R.id.joystickView);
        tvScore = view.findViewById(R.id.tvScore);

        // 绑定分数显示
        snakeView.setScoreTextView(tvScore);

        // 设置摇杆监听
        joystickView.setOnDirectionChangeListener(new JoystickView.OnDirectionChangeListener() {
            @Override
            public void onDirectionChange(int direction) {
                snakeView.setExternalDirection(direction);
            }

            @Override
            public void onDirectionReset() {
                // 可选：手指抬起后暂停游戏？或保持最后方向？
                // 这里我们选择保持最后方向，不暂停
            }
        });

        // 点击游戏区域开始游戏（保留）
        snakeView.setOnClickListener(v -> {
            if (!snakeView.isRunning()) {
                snakeView.startGame();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (snakeView != null) {
            snakeView.resumeGame();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (snakeView != null) {
            snakeView.pauseGame();
        }
    }
}