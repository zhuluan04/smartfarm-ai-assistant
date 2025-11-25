package com.linjiu.recognize.layout.program.game.tetris;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;

import java.util.Random;

public class TetrisView extends View {
    private static final int ROWS = 20;
    private static final int COLS = 10;
    private static final int BLOCK_SIZE = 50;

    public interface OnGameInfoUpdateListener {
        void onGameInfoUpdated(int score, int lines, int level);
        void onGameOver();
    }

    private OnGameInfoUpdateListener gameInfoListener;

    private int[][] board = new int[ROWS][COLS];
    private Tetromino current;
    private Tetromino next;
    private Paint paint = new Paint();
    private static final Random random = new Random(); // ✅ 全局单例，避免伪随机

    private Handler handler;
    private Runnable gameLoop;
    private boolean isGameOver = false;
    private boolean isPaused = false;

    // 游戏状态
    private int score = 0;
    private int linesCleared = 0;
    private int level = 1;
    private long dropInterval = 500;

    // 固定颜色
    private static final int[] TETROMINO_COLORS = {
            Color.CYAN,      // I
            Color.YELLOW,    // O
            Color.MAGENTA,   // T
            Color.BLUE,      // J
            Color.rgb(255, 165, 0), // L (Orange)
            Color.GREEN,     // S
            Color.RED        // Z
    };

    public TetrisView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        handler = new Handler(Looper.getMainLooper()); // ✅ 绑定主线程
        paint.setAntiAlias(true);
        setFocusable(true);
        setFocusableInTouchMode(true);
        startNewGame();
    }

    public void startNewGame() {
        isGameOver = false;
        isPaused = false;
        score = 0;
        linesCleared = 0;
        level = 1;
        dropInterval = 500;

        clearBoard();
        next = Tetromino.randomTetromino();
        spawnTetromino();

        if (gameInfoListener != null) {
            gameInfoListener.onGameInfoUpdated(score, linesCleared, level);
        }

        // ✅ 清理旧循环，防止重复 post
        if (gameLoop != null) {
            handler.removeCallbacks(gameLoop);
        }

        gameLoop = new Runnable() {
            @Override
            public void run() {
                if (!isGameOver && !isPaused) {
                    moveDown();
                    invalidate();
                }
                handler.postDelayed(this, dropInterval);
            }
        };
        handler.post(gameLoop);
    }

    private void clearBoard() {
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = 0;
            }
        }
    }

    private void spawnTetromino() {
        current = next;
        next = Tetromino.randomTetromino();
        current.x = COLS / 2 - current.shape[0].length / 2; // ✅ 居中生成
        current.y = 0;

        if (collides(current.x, current.y, current.shape)) {
            gameOver();
        }
    }

    private void moveDown() {
        if (!collides(current.x, current.y + 1, current.shape)) {
            current.y++;
        } else {
            mergeToBoard();
            int lines = clearLines();
            if (lines > 0) {
                updateScore(lines);
            }
            spawnTetromino();
        }
    }

    private void hardDrop() {
        while (!collides(current.x, current.y + 1, current.shape)) {
            current.y++;
        }
        mergeToBoard();
        int lines = clearLines();
        if (lines > 0) {
            updateScore(lines);
        }
        spawnTetromino();
        invalidate();
    }

    public void controlLeft() {
        if (isGameOver || isPaused || current == null) return;
        if (!collides(current.x - 1, current.y, current.shape)) {
            current.x--;
            invalidate();
        }
    }

    public void controlRight() {
        if (isGameOver || isPaused || current == null) return;
        if (!collides(current.x + 1, current.y, current.shape)) {
            current.x++;
            invalidate();
        }
    }

    public void controlRotate() {
        if (isGameOver || isPaused || current == null) return;
        int[][] rotated = current.rotate();
        if (!collides(current.x, current.y, rotated)) {
            current.shape = rotated;
            invalidate();
            return;
        }
        // 墙踢：右移尝试
        if (!collides(current.x + 1, current.y, rotated)) {
            current.x++;
            current.shape = rotated;
            invalidate();
            return;
        }
        // 墙踢：左移尝试
        if (!collides(current.x - 1, current.y, rotated)) {
            current.x--;
            current.shape = rotated;
            invalidate();
            return;
        }
        // 墙踢：上移尝试（解决天花板卡死）
        if (!collides(current.x, current.y - 1, rotated)) {
            current.y--;
            current.shape = rotated;
            invalidate();
        }
    }

    public void controlDown() {
        if (isGameOver || isPaused || current == null) return;
        moveDown();
        invalidate();
    }

    public void controlHardDrop() {
        if (isGameOver || isPaused || current == null) return;
        hardDrop();
    }

    public void togglePause() {
        if (isGameOver) return;
        isPaused = !isPaused;
        invalidate();
    }

    private boolean collides(int x, int y, int[][] shape) {
        for (int i = 0; i < shape.length; i++) {
            for (int j = 0; j < shape[i].length; j++) {
                if (shape[i][j] != 0) {
                    int newX = x + j;
                    int newY = y + i;
                    if (newX < 0 || newX >= COLS || newY >= ROWS) {
                        return true;
                    }
                    if (newY >= 0 && board[newY][newX] != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void mergeToBoard() {
        for (int i = 0; i < current.shape.length; i++) {
            for (int j = 0; j < current.shape[i].length; j++) {
                if (current.shape[i][j] != 0) {
                    int x = current.x + j;
                    int y = current.y + i;
                    if (y >= 0 && y < ROWS && x >= 0 && x < COLS) {
                        board[y][x] = current.shape[i][j];
                    }
                }
            }
        }
    }

    private int clearLines() {
        int lines = 0;
        for (int i = ROWS - 1; i >= 0; i--) {
            boolean full = true;
            for (int j = 0; j < COLS; j++) {
                if (board[i][j] == 0) {
                    full = false;
                    break;
                }
            }
            if (full) {
                lines++;
                // 下移上面所有行
                for (int k = i; k > 0; k--) {
                    System.arraycopy(board[k - 1], 0, board[k], 0, COLS);
                }
                // 清空顶行
                for (int j = 0; j < COLS; j++) {
                    board[0][j] = 0;
                }
                // ✅ 修复：不执行 i++，避免跳过下一行
                // 继续检查当前行（因为上一行已下移至此）
            }
        }
        return lines;
    }

    private void updateScore(int lines) {
        linesCleared += lines;
        int[] points = {0, 100, 300, 500, 800};
        score += points[lines] * level;

        // 每10行升一级
        int newLevel = linesCleared / 10 + 1;
        if (newLevel > level) {
            level = newLevel;
            dropInterval = Math.max(100, 500 - (level - 1) * 50);
        }

        if (gameInfoListener != null) {
            gameInfoListener.onGameInfoUpdated(score, linesCleared, level);
        }
    }

    private void gameOver() {
        isGameOver = true;
        if (handler != null && gameLoop != null) {
            handler.removeCallbacks(gameLoop); // ✅ 停止游戏循环
        }
        invalidate();

        if (gameInfoListener != null) {
            gameInfoListener.onGameOver(); // ✅ 通知 Fragment，不在 View 里 Toast
        }
    }

    public void pauseGame() {
        isPaused = true;
        invalidate();
    }

    public void resumeGame() {
        isPaused = false;
        invalidate();
    }

    public void restartGame() {
        startNewGame();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (canvas == null) return;

        // 背景
        canvas.drawColor(Color.BLACK);

        // 网格线
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(1);
        for (int i = 0; i <= ROWS; i++) {
            canvas.drawLine(0, i * BLOCK_SIZE, COLS * BLOCK_SIZE, i * BLOCK_SIZE, paint);
        }
        for (int j = 0; j <= COLS; j++) {
            canvas.drawLine(j * BLOCK_SIZE, 0, j * BLOCK_SIZE, ROWS * BLOCK_SIZE, paint);
        }

        // 绘制固定方块
        paint.setStyle(Paint.Style.FILL);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                if (board[i][j] != 0) {
                    paint.setColor(board[i][j]);
                    canvas.drawRect(j * BLOCK_SIZE, i * BLOCK_SIZE,
                            (j + 1) * BLOCK_SIZE, (i + 1) * BLOCK_SIZE, paint);
                }
            }
        }

        // 绘制当前方块
        if (current != null && !isGameOver) {
            for (int i = 0; i < current.shape.length; i++) {
                for (int j = 0; j < current.shape[i].length; j++) {
                    if (current.shape[i][j] != 0) {
                        int drawY = current.y + i;
                        if (drawY >= 0) {
                            paint.setColor(current.shape[i][j]);
                            int x = (current.x + j) * BLOCK_SIZE;
                            int y = drawY * BLOCK_SIZE;
                            canvas.drawRect(x, y, x + BLOCK_SIZE, y + BLOCK_SIZE, paint);
                        }
                    }
                }
            }
        }

        // 绘制状态文字
        paint.setColor(Color.WHITE);
        paint.setTextSize(60);
        paint.setTextAlign(Paint.Align.CENTER);

        if (isGameOver) {
            canvas.drawText("GAME OVER", getWidth() / 2f, getHeight() / 2f, paint);
        } else if (isPaused) {
            canvas.drawText("PAUSED", getWidth() / 2f, getHeight() / 2f, paint);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (isGameOver) return true;

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                controlLeft();
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                controlRight();
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                controlRotate();
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                controlDown();
                return true;
            case KeyEvent.KEYCODE_SPACE:
                controlHardDrop();
                return true;
            case KeyEvent.KEYCODE_P:
                togglePause();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // Getters
    public boolean isGameOver() { return isGameOver; }
    public boolean isPaused() { return isPaused; }
    public void setGameInfoListener(OnGameInfoUpdateListener listener) {
        this.gameInfoListener = listener;
    }

    // Tetromino 类
    static class Tetromino {
        int[][] shape;
        int x, y;

        Tetromino(int[][] shape) {
            this.shape = shape;
        }

        static Tetromino randomTetromino() {
            int[][][] shapes = {
                    {{1, 1, 1, 1}},               // I
                    {{1, 1}, {1, 1}},             // O
                    {{0, 1, 0}, {1, 1, 1}},       // T
                    {{1, 0, 0}, {1, 1, 1}},       // J
                    {{0, 0, 1}, {1, 1, 1}},       // L
                    {{0, 1, 1}, {1, 1, 0}},       // S
                    {{1, 1, 0}, {0, 1, 1}}        // Z
            };

            int idx = random.nextInt(shapes.length); // ✅ 使用全局 Random
            int[][] s = shapes[idx];
            int color = TETROMINO_COLORS[idx];

            int[][] colored = new int[s.length][s[0].length];
            for (int i = 0; i < s.length; i++) {
                for (int j = 0; j < s[i].length; j++) {
                    colored[i][j] = s[i][j] == 1 ? color : 0;
                }
            }

            return new Tetromino(colored);
        }

        int[][] rotate() {
            int m = shape.length, n = shape[0].length;
            int[][] rotated = new int[n][m];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    rotated[j][m - 1 - i] = shape[i][j];
                }
            }
            return rotated;
        }
    }
}