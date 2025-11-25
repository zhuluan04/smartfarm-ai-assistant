// JoystickView.java
package com.linjiu.recognize.layout.program.game.snake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class JoystickView extends View {

    private float centerX, centerY, radius;
    private float knobX, knobY, knobRadius;
    private float touchX, touchY;
    private boolean isPressed = false;

    private Paint backgroundPaint, knobPaint;
    private OnDirectionChangeListener listener;


    public JoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        backgroundPaint = new Paint();
        backgroundPaint.setColor(Color.argb(100, 255, 255, 255));
        backgroundPaint.setStyle(Paint.Style.FILL);

        knobPaint = new Paint();
        knobPaint.setColor(Color.WHITE);
        knobPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        centerX = w / 2f;
        centerY = h / 2f;
        radius = Math.min(centerX, centerY) * 0.8f;
        knobRadius = radius * 0.3f;
        resetKnob();
    }

    private void resetKnob() {
        knobX = centerX;
        knobY = centerY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 绘制背景圆
        canvas.drawCircle(centerX, centerY, radius, backgroundPaint);
        // 绘制摇杆
        canvas.drawCircle(knobX, knobY, knobRadius, knobPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                touchX = event.getX();
                touchY = event.getY();
                updateKnobPosition();
                if (listener != null) {
                    listener.onDirectionChange(calculateDirection());
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isPressed = false;
                resetKnob();
                if (listener != null) {
                    listener.onDirectionReset();
                }
                invalidate();
                return true;
        }
        return super.onTouchEvent(event);
    }

    private void updateKnobPosition() {
        float dx = touchX - centerX;
        float dy = touchY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance < radius) {
            knobX = touchX;
            knobY = touchY;
        } else {
            // 吸附到圆周上
            float ratio = radius / distance;
            knobX = centerX + dx * ratio;
            knobY = centerY + dy * ratio;
        }
    }

    // 计算方向：0=右, 1=下, 2=左, 3=上, -1=无效/居中
    private int calculateDirection() {
        float dx = knobX - centerX;
        float dy = knobY - centerY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        // 死区：小于半径10%不触发方向
        if (distance < radius * 0.1f) {
            return -1; // 无方向
        }

        float angleRad = (float) Math.atan2(dy, dx);
        float angleDeg = (float) Math.toDegrees(angleRad);
        if (angleDeg < 0) angleDeg += 360;

        if (angleDeg >= 315 || angleDeg < 45) return 0;
        if (angleDeg >= 45 && angleDeg < 135) return 1;
        if (angleDeg >= 135 && angleDeg < 225) return 2;
        if (angleDeg >= 225 && angleDeg < 315) return 3;

        return -1;
    }



    public void setOnDirectionChangeListener(OnDirectionChangeListener listener) {
        this.listener = listener;
    }

    public interface OnDirectionChangeListener {
        void onDirectionChange(int direction); // 0,1,2,3
        void onDirectionReset(); // 手指抬起，可选暂停或保持最后方向
    }
}