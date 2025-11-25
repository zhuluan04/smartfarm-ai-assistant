// SnakeView.java
package com.linjiu.recognize.layout.program.game.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnakeView extends View {

    private static final int GRID_SIZE = 20; // 每格像素大小
    private static final int INITIAL_SNAKE_LENGTH = 3;

    private int widthInGrid, heightInGrid; // 网格行列数
    private List<Point> snake; // 蛇身，头在 list 最后
    private Point food;
    private int direction; // 0=右, 1=下, 2=左, 3=上
    private boolean isRunning = false;
    private int score = 0;

    private Paint snakePaint, foodPaint, textPaint;
    private Random random = new Random();
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable gameLoop = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                update();
                invalidate(); // 重绘
                handler.postDelayed(this, 200); // 控制速度，毫秒
            }
        }
    };

    public static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Point) {
                Point p = (Point) obj;
                return p.x == this.x && p.y == this.y;
            }
            return false;
        }
    }

    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        snakePaint = new Paint();
        snakePaint.setColor(Color.GREEN);
        snakePaint.setStyle(Paint.Style.FILL);

        foodPaint = new Paint();
        foodPaint.setColor(Color.RED);
        foodPaint.setStyle(Paint.Style.FILL);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(60);
        textPaint.setTextAlign(Paint.Align.CENTER);

        setFocusable(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        widthInGrid = w / GRID_SIZE;
        heightInGrid = h / GRID_SIZE;
        resetGame();
    }

    public void startGame() {
        resetGame();
        isRunning = true;
        handler.post(gameLoop);
        this.requestFocus(); // 获取焦点，避免按键冲突
    }

    public void pauseGame() {
        isRunning = false;
        handler.removeCallbacks(gameLoop);
    }

    public void resumeGame() {
        if (!isRunning) {
            isRunning = true;
            handler.post(gameLoop);
        }
    }

    private void resetGame() {
        snake = new ArrayList<>();
        // 初始化蛇身（横向）
        for (int i = 0; i < INITIAL_SNAKE_LENGTH; i++) {
            snake.add(new Point(i, 0));
        }
        direction = 0; // 向右
        spawnFood();
        score = 0;
        isRunning = false;
    }

    private void spawnFood() {
        int x, y;
        do {
            x = random.nextInt(widthInGrid);
            y = random.nextInt(heightInGrid);
        } while (snake.contains(new Point(x, y))); // 不能生成在蛇身上
        food = new Point(x, y);
    }

    private void update() {
        Point head = snake.get(snake.size() - 1);
        Point newHead = new Point(head.x, head.y);

        // 根据方向移动
        switch (direction) {
            case 0: newHead.x++; break; // 右
            case 1: newHead.y++; break; // 下
            case 2: newHead.x--; break; // 左
            case 3: newHead.y--; break; // 上
        }

        // 检查是否撞墙
        if (newHead.x < 0 || newHead.x >= widthInGrid ||
                newHead.y < 0 || newHead.y >= heightInGrid) {
            gameOver();
            return;
        }

        // 检查是否撞自己
        if (snake.contains(newHead)) {
            gameOver();
            return;
        }

        // 添加新头
        snake.add(newHead);

        // 检查是否吃到食物
        if (newHead.equals(food)) {
            score += 10;
            spawnFood();
        } else {
            // 没吃到，移除尾巴
            snake.remove(0);
        }
    }

    // 新增方法：供外部调用改变方向
    public void changeDirection(int newDirection) {
        // 防止180度掉头
//        if ((newDirection == 0 && direction != 2) || // 右，不能从左来
//                (newDirection == 2 && direction != 0) || // 左，不能从右来
//                (newDirection == 1 && direction != 3) || // 下，不能从上来
//                (newDirection == 3 && direction != 1)) { // 上，不能从下来
//            direction = newDirection;
//        }
        if (Math.abs(newDirection - direction) != 2) { // 0-右, 1-下, 2-左, 3-上
            direction = newDirection;
        }
    }

    // 新增方法：供 Fragment 查询游戏状态
    public boolean isRunning() {
        return isRunning;
    }

    private void gameOver() {
        isRunning = false;
        // 可以弹出对话框或显示“Game Over”
        // 这里简单重置
        postDelayed(this::resetGame, 1000);
    }

    // SnakeView.java
    public void setExternalDirection(int direction) {
        // 防止180度掉头
        if ((this.direction == 0 && direction == 2) ||
                (this.direction == 2 && direction == 0) ||
                (this.direction == 1 && direction == 3) ||
                (this.direction == 3 && direction == 1)) {
            return;
        }
        this.direction = direction;
    }

    // 添加分数更新接口（可选）
    public void setScoreTextView(android.widget.TextView tv) {
        this.scoreTextView = tv; // 你需要先声明这个成员变量
    }

    private android.widget.TextView scoreTextView;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.BLACK);

        // 绘制蛇
        for (Point p : snake) {
            canvas.drawRect(
                    p.x * GRID_SIZE,
                    p.y * GRID_SIZE,
                    (p.x + 1) * GRID_SIZE,
                    (p.y + 1) * GRID_SIZE,
                    snakePaint
            );
        }

        // 绘制食物
        if (food != null) {
            canvas.drawRect(
                    food.x * GRID_SIZE,
                    food.y * GRID_SIZE,
                    (food.x + 1) * GRID_SIZE,
                    (food.y + 1) * GRID_SIZE,
                    foodPaint
            );
        }

        // 更新分数显示（如果绑定了 TextView）
        if (scoreTextView != null) {
            scoreTextView.setText("Score: " + score);
        }
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            if (!isRunning) {
//                startGame();
//            } else {
//                // 滑动控制方向（简化版：按屏幕左右上下区域）
//                float x = event.getX();
//                float y = event.getY();
//                float centerX = getWidth() / 2f;
//                float centerY = getHeight() / 2f;
//
//                if (Math.abs(x - centerX) > Math.abs(y - centerY)) {
//                    // 左右滑动
//                    if (x < centerX && direction != 0) direction = 2; // 左
//                    else if (x > centerX && direction != 2) direction = 0; // 右
//                } else {
//                    // 上下滑动
//                    if (y < centerY && direction != 1) direction = 3; // 上
//                    else if (y > centerY && direction != 3) direction = 1; // 下
//                }
//            }
//            return true;
//        }
//        return super.onTouchEvent(event);
//    }


}