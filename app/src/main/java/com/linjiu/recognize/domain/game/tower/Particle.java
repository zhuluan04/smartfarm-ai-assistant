package com.linjiu.recognize.domain.game.tower;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

// 攻击粒子
public class Particle {
    public float x, y;
    private float vx, vy;
    private int life;
    private int maxLife = 30;
    private int color;

    public Particle(float x, float y) {
        this.x = x;
        this.y = y;
        this.vx = (float) (Math.random() * 8 - 4);
        this.vy = (float) (Math.random() * 8 - 4);
        this.life = maxLife;
        this.color = Color.YELLOW;
    }

    public void update() {
        x += vx;
        y += vy;
        vy += 0.2f; // 重力
        life--;
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void draw(Canvas canvas, Paint paint) {
        int alpha = (int) (255 * ((float) life / maxLife));
        paint.setColor((alpha << 24) | (color & 0x00FFFFFF));
        canvas.drawCircle(x, y, 4, paint);
    }
}