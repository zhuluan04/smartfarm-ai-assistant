package com.linjiu.recognize.layout.program.game;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MouseGameFragment extends Fragment {

    private ImageView ivMouse;
    private TextView tvScore;
    private int score = 0;

    private final Handler handler = new Handler();
    private final Random random = new Random();

    // 保存九个洞的中心点坐标
    private final List<Point> holeCenters = new ArrayList<>();

    private final Runnable gameRunnable = new Runnable() {
        @Override
        public void run() {
            showRandomMouse();
            handler.postDelayed(this, 1000); // 每 1 秒刷新一次老鼠
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        requireActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        return inflater.inflate(R.layout.fragment_game_mouse, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivMouse = view.findViewById(R.id.iv_mouse);
        tvScore = view.findViewById(R.id.tv_score);

        ivMouse.setOnClickListener(v -> {
            score++;
            tvScore.setText("Score: " + score);
            ivMouse.setVisibility(View.GONE); // 打到后立即消失

            // 输出老鼠当前的坐标和尺寸
            float x = ivMouse.getX();
            float y = ivMouse.getY();
            int w = ivMouse.getWidth();
            int h = ivMouse.getHeight();
            float centerX = x + w / 2f;
            float centerY = y + h / 2f;

            Log.d("MouseGame", "点击老鼠位置: 左上角(" + x + ", " + y +
                    "), 尺寸(" + w + "x" + h +
                    "), 中心点(" + centerX + ", " + centerY + ")");
        });

        // 等待布局完成，计算洞口位置
        view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        calculateHoleCenters(view);
                    }
                });
    }

    /**
     * 计算 3x3 九个洞的中心点位置
     */
    private void calculateHoleCenters(View root) {
        holeCenters.clear();

        View parent = (View) ivMouse.getParent();
        int parentW = parent.getWidth();
        int parentH = parent.getHeight();

        // 如果布局尚未完成，宽高可能为0，此时应避免计算
        if (parentW <= 0 || parentH <= 0) {
            Log.w("MouseGame", "Parent size is zero, cannot calculate hole centers yet.");
            return;
        }

        int cellW = parentW / 3;
        int cellH = parentH / 3;

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                int centerX = c * cellW + cellW / 2;
                int centerY = r * cellH + cellH / 2;
                holeCenters.add(new Point(centerX, centerY));
            }
        }

        // 使用 Log 输出到 Logcat
        StringBuilder sb = new StringBuilder("洞口坐标：\n");
        for (int i = 0; i < holeCenters.size(); i++) {
            Point p = holeCenters.get(i);
            sb.append("洞").append(i + 1).append(": (")
                    .append(p.x).append(", ").append(p.y).append(")\n");
        }
        Log.d("MouseGame", sb.toString());
    }

    /**
     * 显示随机地鼠
     */
    private void showRandomMouse() {
        if (ivMouse == null || holeCenters.isEmpty()) return;

        int idx = random.nextInt(holeCenters.size());
        Point center = holeCenters.get(idx);

        int mouseW = ivMouse.getWidth();
        int mouseH = ivMouse.getHeight();

        int x = center.x - mouseW / 2;
        int y = center.y - mouseH / 2;

        ivMouse.setX(x);
        ivMouse.setY(y);
        ivMouse.setVisibility(View.VISIBLE);

        Log.d("MouseGame", "老鼠刷新到洞" + (idx + 1) + "，中心点(" + center.x + ", " + center.y + ")");
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        handler.post(gameRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(gameRunnable);
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
}
