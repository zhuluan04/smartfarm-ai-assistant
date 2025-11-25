package com.linjiu.recognize.layout.program.game;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.linjiu.recognize.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// 2048游戏页面
public class Game2048Fragment extends Fragment {

    private static final int GRID_SIZE = 4;
    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private int[][] gameBoard = new int[GRID_SIZE][GRID_SIZE];
    private TextView[][] tileViews = new TextView[GRID_SIZE][GRID_SIZE];
    private GridLayout gameGrid;
    private TextView scoreText;
    private TextView bestScoreText;
    private Button newGameButton;
    private int score = 0;
    private int bestScore = 0;
    private boolean gameOver = false;
    private GestureDetector gestureDetector;
    private Random random = new Random();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 使用代码创建布局而不是从XML加载
        return createGameLayout();
    }

    private View createGameLayout() {
        LinearLayout rootLayout = new LinearLayout(getContext());
        rootLayout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setBackgroundColor(Color.parseColor("#FAF8EF"));

        // 添加Toolbar
        Toolbar toolbar = new Toolbar(getContext());
        toolbar.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                getResources().getDimensionPixelSize(R.dimen.toolbar_height)
        ));
        toolbar.setBackgroundColor(Color.parseColor("#8F7A66"));
        toolbar.setTitle("2048");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        rootLayout.addView(toolbar);

        // 游戏容器
        LinearLayout gameContainer = new LinearLayout(getContext());
        LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1
        );
        containerParams.setMargins(16, 16, 16, 16);
        gameContainer.setLayoutParams(containerParams);
        gameContainer.setOrientation(LinearLayout.VERTICAL);
        rootLayout.addView(gameContainer);

        // 分数板块
        LinearLayout scoreContainer = new LinearLayout(getContext());
        scoreContainer.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        scoreContainer.setOrientation(LinearLayout.HORIZONTAL);
        scoreContainer.setGravity(Gravity.CENTER);
        gameContainer.addView(scoreContainer);

        // 当前分数
        LinearLayout currentScoreLayout = createScoreLayout("分数", "0");
        scoreText = (TextView) ((LinearLayout) currentScoreLayout).getChildAt(1);
        scoreContainer.addView(currentScoreLayout);

        // 间隔
        View spacer = new View(getContext());
        spacer.setLayoutParams(new LinearLayout.LayoutParams(32, 0));
        scoreContainer.addView(spacer);

        // 最高分
        LinearLayout bestScoreLayout = createScoreLayout("最高分", "0");
        bestScoreText = (TextView) ((LinearLayout) bestScoreLayout).getChildAt(1);
        scoreContainer.addView(bestScoreLayout);

        // 新游戏按钮
        newGameButton = new Button(getContext());
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        buttonParams.gravity = Gravity.CENTER;
        buttonParams.setMargins(0, 20, 0, 20);
        newGameButton.setLayoutParams(buttonParams);
        newGameButton.setText("新游戏");
        newGameButton.setBackgroundColor(Color.parseColor("#8F7A66"));
        newGameButton.setTextColor(Color.WHITE);
        newGameButton.setPadding(32, 16, 32, 16);
        newGameButton.setOnClickListener(v -> startNewGame());
        gameContainer.addView(newGameButton);

        // 游戏网格容器
        CardView gridCard = new CardView(getContext());
        LinearLayout.LayoutParams gridCardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        gridCardParams.gravity = Gravity.CENTER;
        gridCard.setLayoutParams(gridCardParams);
        gridCard.setCardBackgroundColor(Color.parseColor("#BBADA0"));
        gridCard.setRadius(12);
        gridCard.setCardElevation(8);
        gameContainer.addView(gridCard);

        // 游戏网格
        gameGrid = new GridLayout(getContext());
        gameGrid.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        gameGrid.setRowCount(GRID_SIZE);
        gameGrid.setColumnCount(GRID_SIZE);
        gameGrid.setPadding(8, 8, 8, 8);
        gridCard.addView(gameGrid);

        return rootLayout;
    }

    private LinearLayout createScoreLayout(String label, String value) {
        LinearLayout layout = new LinearLayout(getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(16, 8, 16, 8);
        layout.setLayoutParams(params);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#BBADA0"));
        layout.setPadding(24, 16, 24, 16);
        layout.setGravity(Gravity.CENTER);

        TextView labelText = new TextView(getContext());
        labelText.setText(label);
        labelText.setTextColor(Color.parseColor("#EEE4DA"));
        labelText.setTextSize(14);
        layout.addView(labelText);

        TextView valueText = new TextView(getContext());
        valueText.setText(value);
        valueText.setTextColor(Color.WHITE);
        valueText.setTextSize(24);
        valueText.setTypeface(null, Typeface.BOLD);
        layout.addView(valueText);

        return layout;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 加载最高分
        loadBestScore();

        // 初始化手势检测器
        gestureDetector = new GestureDetector(getContext(), new SwipeGestureListener());

        // 设置触摸监听器
        view.setOnTouchListener((v, event) -> {
            gestureDetector.onTouchEvent(event);
            return true;
        });

        // 初始化游戏
        initializeGrid();
        startNewGame();
    }

    private void initializeGrid() {
        int tileSize = getScreenWidth() / (GRID_SIZE + 1);

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                TextView tile = new TextView(getContext());
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = tileSize;
                params.height = tileSize;
                params.rowSpec = GridLayout.spec(i);
                params.columnSpec = GridLayout.spec(j);
                params.setMargins(4, 4, 4, 4);
                tile.setLayoutParams(params);
                tile.setGravity(Gravity.CENTER);
                tile.setTextSize(24);
                tile.setTypeface(null, Typeface.BOLD);
                tile.setBackgroundColor(Color.parseColor("#CDC1B4"));

                tileViews[i][j] = tile;
                gameGrid.addView(tile);
            }
        }
    }

    private int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    private void startNewGame() {
        gameOver = false;
        score = 0;
        updateScore();

        // 清空游戏板
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                gameBoard[i][j] = 0;
            }
        }

        // 添加两个初始方块
        addRandomTile();
        addRandomTile();
        updateUI();
    }

    private void addRandomTile() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (gameBoard[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
            gameBoard[cell[0]][cell[1]] = random.nextInt(10) < 9 ? 2 : 4;
            animateTileAppear(cell[0], cell[1]);
        }
    }

    private void updateUI() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                updateTile(i, j);
            }
        }
    }

    private void updateTile(int row, int col) {
        TextView tile = tileViews[row][col];
        int value = gameBoard[row][col];

        if (value == 0) {
            tile.setText("");
            tile.setBackgroundColor(Color.parseColor("#CDC1B4"));
        } else {
            tile.setText(String.valueOf(value));
            tile.setBackgroundColor(getTileColor(value));
            tile.setTextColor(value <= 4 ? Color.parseColor("#776E65") : Color.WHITE);

            // 调整字体大小
            if (value < 100) {
                tile.setTextSize(32);
            } else if (value < 1000) {
                tile.setTextSize(28);
            } else {
                tile.setTextSize(24);
            }
        }
    }

    private int getTileColor(int value) {
        switch (value) {
            case 2: return Color.parseColor("#EEE4DA");
            case 4: return Color.parseColor("#EDE0C8");
            case 8: return Color.parseColor("#F2B179");
            case 16: return Color.parseColor("#F59563");
            case 32: return Color.parseColor("#F67C5F");
            case 64: return Color.parseColor("#F65E3B");
            case 128: return Color.parseColor("#EDCF72");
            case 256: return Color.parseColor("#EDCC61");
            case 512: return Color.parseColor("#EDC850");
            case 1024: return Color.parseColor("#EDC53F");
            case 2048: return Color.parseColor("#EDC22E");
            default: return Color.parseColor("#3C3A32");
        }
    }

    private void animateTileAppear(int row, int col) {
        TextView tile = tileViews[row][col];
        tile.setScaleX(0);
        tile.setScaleY(0);
        tile.animate()
                .scaleX(1)
                .scaleY(1)
                .setDuration(200)
                .start();
    }

    private boolean move(Direction direction) {
        boolean moved = false;
        int[][] previousBoard = copyBoard();

        switch (direction) {
            case UP:
                moved = moveUp();
                break;
            case DOWN:
                moved = moveDown();
                break;
            case LEFT:
                moved = moveLeft();
                break;
            case RIGHT:
                moved = moveRight();
                break;
        }

        if (moved) {
            addRandomTile();
            updateUI();

            if (checkWin()) {
                showWinDialog();
            } else if (checkGameOver()) {
                gameOver = true;
                showGameOverDialog();
            }
        }

        return moved;
    }

    private boolean moveLeft() {
        boolean moved = false;
        for (int i = 0; i < GRID_SIZE; i++) {
            int[] row = gameBoard[i];
            int[] newRow = mergeLine(row);
            if (!arraysEqual(row, newRow)) {
                gameBoard[i] = newRow;
                moved = true;
            }
        }
        return moved;
    }

    private boolean moveRight() {
        boolean moved = false;
        for (int i = 0; i < GRID_SIZE; i++) {
            int[] row = reverseArray(gameBoard[i]);
            int[] newRow = mergeLine(row);
            newRow = reverseArray(newRow);
            if (!arraysEqual(gameBoard[i], newRow)) {
                gameBoard[i] = newRow;
                moved = true;
            }
        }
        return moved;
    }

    private boolean moveUp() {
        boolean moved = false;
        for (int j = 0; j < GRID_SIZE; j++) {
            int[] column = new int[GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                column[i] = gameBoard[i][j];
            }
            int[] newColumn = mergeLine(column);
            if (!arraysEqual(column, newColumn)) {
                for (int i = 0; i < GRID_SIZE; i++) {
                    gameBoard[i][j] = newColumn[i];
                }
                moved = true;
            }
        }
        return moved;
    }

    private boolean moveDown() {
        boolean moved = false;
        for (int j = 0; j < GRID_SIZE; j++) {
            int[] column = new int[GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                column[i] = gameBoard[GRID_SIZE - 1 - i][j];
            }
            int[] newColumn = mergeLine(column);
            if (!arraysEqual(column, newColumn)) {
                for (int i = 0; i < GRID_SIZE; i++) {
                    gameBoard[GRID_SIZE - 1 - i][j] = newColumn[i];
                }
                moved = true;
            }
        }
        return moved;
    }

    private int[] mergeLine(int[] line) {
        int[] result = new int[GRID_SIZE];
        int resultIndex = 0;

        // 先移动所有非零元素
        for (int i = 0; i < GRID_SIZE; i++) {
            if (line[i] != 0) {
                result[resultIndex++] = line[i];
            }
        }

        // 合并相同的相邻元素
        for (int i = 0; i < GRID_SIZE - 1; i++) {
            if (result[i] != 0 && result[i] == result[i + 1]) {
                result[i] *= 2;
                score += result[i];
                updateScore();

                // 移动后续元素
                for (int j = i + 1; j < GRID_SIZE - 1; j++) {
                    result[j] = result[j + 1];
                }
                result[GRID_SIZE - 1] = 0;
            }
        }

        return result;
    }

    private int[] reverseArray(int[] array) {
        int[] result = new int[array.length];
        for (int i = 0; i < array.length; i++) {
            result[i] = array[array.length - 1 - i];
        }
        return result;
    }

    private boolean arraysEqual(int[] a, int[] b) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) return false;
        }
        return true;
    }

    private int[][] copyBoard() {
        int[][] copy = new int[GRID_SIZE][GRID_SIZE];
        for (int i = 0; i < GRID_SIZE; i++) {
            System.arraycopy(gameBoard[i], 0, copy[i], 0, GRID_SIZE);
        }
        return copy;
    }

    private boolean checkWin() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (gameBoard[i][j] == 2048) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkGameOver() {
        // 检查是否有空格
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (gameBoard[i][j] == 0) {
                    return false;
                }
            }
        }

        // 检查是否可以合并
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int current = gameBoard[i][j];
                if ((i < GRID_SIZE - 1 && current == gameBoard[i + 1][j]) ||
                        (j < GRID_SIZE - 1 && current == gameBoard[i][j + 1])) {
                    return false;
                }
            }
        }

        return true;
    }

    private void updateScore() {
        scoreText.setText(String.valueOf(score));
        if (score > bestScore) {
            bestScore = score;
            bestScoreText.setText(String.valueOf(bestScore));
            saveBestScore();
        }
    }

    private void loadBestScore() {
        SharedPreferences prefs = getActivity().getSharedPreferences("Game2048", Context.MODE_PRIVATE);
        bestScore = prefs.getInt("bestScore", 0);
        bestScoreText.setText(String.valueOf(bestScore));
    }

    private void saveBestScore() {
        SharedPreferences prefs = getActivity().getSharedPreferences("Game2048", Context.MODE_PRIVATE);
        prefs.edit().putInt("bestScore", bestScore).apply();
    }

    private void showWinDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("恭喜！")
                .setMessage("你达到了2048！是否继续游戏？")
                .setPositiveButton("继续", (dialog, which) -> {})
                .setNegativeButton("新游戏", (dialog, which) -> startNewGame())
                .show();
    }

    private void showGameOverDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("游戏结束")
                .setMessage("最终得分: " + score)
                .setPositiveButton("新游戏", (dialog, which) -> startNewGame())
                .show();
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT
    }

    private class SwipeGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (gameOver) return false;

            float deltaX = e2.getX() - e1.getX();
            float deltaY = e2.getY() - e1.getY();

            if (Math.abs(deltaX) > Math.abs(deltaY)) {
                if (Math.abs(deltaX) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (deltaX > 0) {
                        move(Direction.RIGHT);
                    } else {
                        move(Direction.LEFT);
                    }
                    return true;
                }
            } else {
                if (Math.abs(deltaY) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    if (deltaY > 0) {
                        move(Direction.DOWN);
                    } else {
                        move(Direction.UP);
                    }
                    return true;
                }
            }

            return false;
        }
    }
}