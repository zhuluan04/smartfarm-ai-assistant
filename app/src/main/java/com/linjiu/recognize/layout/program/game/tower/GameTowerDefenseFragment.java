package com.linjiu.recognize.layout.program.game.tower;

import androidx.fragment.app.Fragment;

// 塔防大作战 - 塔防游戏
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.linjiu.recognize.R;
import com.linjiu.recognize.domain.game.tower.tower.ArrowTower;
import com.linjiu.recognize.domain.game.tower.tower.CannonTower;
import com.linjiu.recognize.domain.game.tower.tower.MagicTower;
import com.linjiu.recognize.domain.game.tower.tower.Tower;
import com.linjiu.recognize.domain.game.tower.tower.TowerType;

import java.util.ArrayList;
import java.util.List;

// 塔防
public class GameTowerDefenseFragment extends Fragment {

    // 游戏界面
    private GameSurfaceView gameSurfaceView;

    private View rootView;

    // 文字显示
    private TextView tvLives, tvGold, tvWave;

    // 暂停和重新开始按钮
    private Button btnPause, btnRestart;

    // 塔选择面板
    private View towerSelectionPanel;

    // 塔防
    private List<Tower> towers = new ArrayList<>();

    // 塔防选择类型
    private Button btnArrow, btnCannon, btnMagic, btnCancel;

    private int selectedX, selectedY;

    /**
     * 开始时
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_game_tower_defense, container, false);

        // 初始化View
        initViews(rootView);
        setupListeners();

        // 确保 SurfaceView 已经初始化再调用
        if (gameSurfaceView != null) {
            gameSurfaceView.startNextWave();
        }

        return rootView;
    }

    // 建造塔防
    public void buildTower(TowerType type, int x, int y) {
        Tower newTower = null;
        switch (type) {
            case ARROW:
                newTower = new ArrowTower(x, y);
                break;
            case CANNON:
                newTower = new CannonTower(x, y);
                break;
            case MAGIC:
                newTower = new MagicTower(x, y);
                break;
        }
        if (newTower != null) {
            towers.add(newTower);
        }
    }


    /**
     * 初始化视图
     * @param view 视图
     */
    private void initViews(View view) {
        gameSurfaceView = view.findViewById(R.id.gameSurfaceView);
        tvLives = view.findViewById(R.id.tvLives);
        tvGold = view.findViewById(R.id.tvGold);
        tvWave = view.findViewById(R.id.tvWave);
        btnPause = view.findViewById(R.id.btnPause);
        btnRestart = view.findViewById(R.id.btnRestart);

        towerSelectionPanel = view.findViewById(R.id.towerSelectionPanel);
        btnArrow = view.findViewById(R.id.btnArrowTower);
        btnCannon = view.findViewById(R.id.btnCannonTower);
        btnMagic = view.findViewById(R.id.btnMagicTower);
        btnCancel = view.findViewById(R.id.btnCancel);
    }

    // 游戏状态监听
    private void setupListeners() {
        btnPause.setOnClickListener(v -> {
            Toast.makeText(getContext(), "游戏暂停", Toast.LENGTH_SHORT).show();
        });

        btnRestart.setOnClickListener(v -> {
            if (gameSurfaceView != null) {
                gameSurfaceView.resetGame();
                updateHUD();
                gameSurfaceView.startNextWave();
            }
        });

        btnArrow.setOnClickListener(v -> gameSurfaceView.enterBuildingMode(TowerType.ARROW));
        btnCannon.setOnClickListener(v -> gameSurfaceView.enterBuildingMode(TowerType.CANNON));
        btnMagic.setOnClickListener(v -> gameSurfaceView.enterBuildingMode(TowerType.MAGIC));
        btnCancel.setOnClickListener(v -> gameSurfaceView.exitBuildingMode());

        // 设置延迟等待加载，只保留一次
        rootView.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateHUD();
                if (gameSurfaceView != null && gameSurfaceView.isShown()) {
                    rootView.postDelayed(this, 500);
                }
            }
        }, 500);

        gameSurfaceView.setOnTowerPlaceListener((x, y) -> {
            selectedX = x;
            selectedY = y;
            towerSelectionPanel.setVisibility(View.VISIBLE);
        });

        btnArrow.setOnClickListener(v -> {
            gameSurfaceView.buildTower(TowerType.ARROW, selectedX, selectedY);
            towerSelectionPanel.setVisibility(View.GONE);
        });

        btnCannon.setOnClickListener(v -> {
            gameSurfaceView.buildTower(TowerType.CANNON, selectedX, selectedY);
            towerSelectionPanel.setVisibility(View.GONE);
        });

        btnMagic.setOnClickListener(v -> {
            gameSurfaceView.buildTower(TowerType.MAGIC, selectedX, selectedY);
            towerSelectionPanel.setVisibility(View.GONE);
        });

        btnCancel.setOnClickListener(v -> {
            towerSelectionPanel.setVisibility(View.GONE);
        });

    }


    private void updateHUD() {
        // 空指针保护
        if (gameSurfaceView == null) return;

        tvLives.setText("生命: " + gameSurfaceView.getPlayerLives());
        tvGold.setText("金币: " + gameSurfaceView.getPlayerGold());
        tvWave.setText("波次: " + gameSurfaceView.getCurrentWave() + "/" + gameSurfaceView.getMaxWaves());
    }


    // 游戏结束
    public void showGameOver() {
        Toast.makeText(getContext(), "游戏结束！", Toast.LENGTH_LONG).show();
    }

    // 游戏胜利
    public void showVictory() {
        Toast.makeText(getContext(), "恭喜胜利！", Toast.LENGTH_LONG).show();
    }
}