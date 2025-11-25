package com.linjiu.recognize.layout.program.game.mario;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {
    private Paint basePaint, knobPaint;
    private float centerX, centerY;
    private float baseRadius, knobRadius;
    private float knobX, knobY;
    private OnJoystickMoveListener listener;
    private Direction currentDirection = Direction.CENTER;

    public enum Direction {
        CENTER, UP, DOWN, LEFT, RIGHT,
        UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
    }

    public JoystickView(Context ctx) { this(ctx, null); }
    public JoystickView(Context ctx, AttributeSet attrs) { super(ctx, attrs); init(); }

    private void init() {
        basePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        basePaint.setColor(Color.parseColor("#66000000")); // 半透明底盘
        basePaint.setStyle(Paint.Style.FILL);

        knobPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        knobPaint.setColor(Color.parseColor("#FFFFFFFF")); // 摇杆球体（白）
        knobPaint.setStyle(Paint.Style.FILL);
        knobPaint.setShadowLayer(6f, 0, 2f, 0x33000000);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        centerX = w / 2f;
        centerY = h / 2f;
        baseRadius = Math.min(w, h) / 2f * 0.95f;
        knobRadius = baseRadius * 0.42f;
        resetKnob();
    }

    private void resetKnob() {
        knobX = centerX;
        knobY = centerY;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 底盘
        canvas.drawCircle(centerX, centerY, baseRadius, basePaint);
        // 摇杆球
        canvas.drawCircle(knobX, knobY, knobRadius, knobPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE: {
                float dx = x - centerX;
                float dy = y - centerY;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                float max = baseRadius - knobRadius;
                if (dist > max && dist > 0) {
                    dx = dx / dist * max;
                    dy = dy / dist * max;
                }
                knobX = centerX + dx;
                knobY = centerY + dy;

                if (listener != null) {
                    float nx = dx / max; // -1 .. 1
                    float ny = dy / max; // -1 .. 1
                    listener.onMove(nx, ny);
                    Direction newDir = calcDirection(nx, ny);
                    if (newDir != currentDirection) {
                        currentDirection = newDir;
                        listener.onDirectionChanged(newDir);
                    }
                }
                invalidate();
                return true;
            }
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                resetKnob();
                if (listener != null) {
                    listener.onMove(0f, 0f);
                    currentDirection = Direction.CENTER;
                    listener.onDirectionChanged(currentDirection);
                }
                return true;
        }
        return super.onTouchEvent(event);
    }

    private Direction calcDirection(float nx, float ny) {
        float dead = 0.25f; // 低于阈值视为静止
        if (Math.hypot(nx, ny) < dead) return Direction.CENTER;
        double angle = Math.toDegrees(Math.atan2(-ny, nx)); // 右为0度，上为90
        if (angle < 0) angle += 360;
        if (angle >= 337.5 || angle < 22.5) return Direction.RIGHT;
        if (angle >= 22.5 && angle < 67.5) return Direction.UP_RIGHT;
        if (angle >= 67.5 && angle < 112.5) return Direction.UP;
        if (angle >= 112.5 && angle < 157.5) return Direction.UP_LEFT;
        if (angle >= 157.5 && angle < 202.5) return Direction.LEFT;
        if (angle >= 202.5 && angle < 247.5) return Direction.DOWN_LEFT;
        if (angle >= 247.5 && angle < 292.5) return Direction.DOWN;
        return Direction.DOWN_RIGHT;
    }

    public void setOnJoystickMoveListener(OnJoystickMoveListener l) {
        this.listener = l;
    }

    public interface OnJoystickMoveListener {
        /**
         * @param xPercent 水平百分比（-1 左, 0 中, +1 右）
         * @param yPercent 垂直百分比（-1 上, 0 中, +1 下）
         */
        void onMove(float xPercent, float yPercent);

        /**
         * 八向离散方向事件
         */
        void onDirectionChanged(Direction dir);
    }
}
