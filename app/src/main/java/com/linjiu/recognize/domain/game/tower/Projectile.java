package com.linjiu.recognize.domain.game.tower;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.linjiu.recognize.domain.game.tower.enemy.Enemy;

// 抛物线
public class Projectile {

    public float x, y;

    private Enemy target;

    private float speed;

    private int damage;

    private int color;

    public Projectile(float startX, float startY, Enemy target, int damage, float speed, int color) {
        this.x = startX;
        this.y = startY;
        this.target = target;
        this.damage = damage;
        this.speed = speed;
        this.color = color;
    }

    public void update() {
        if (getTarget() != null && !getTarget().isDead()) {
            float dx = getTarget().x - x;
            float dy = getTarget().y - y;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 5) {
                x += dx / dist * speed;
                y += dy / dist * speed;
            }
        }
    }

    public boolean collidesWith(Enemy enemy) {
        return getTarget() == enemy && distanceTo(enemy) < 20;
    }

    private float distanceTo(Enemy e) {
        return (float) Math.sqrt(Math.pow(x - e.x, 2) + Math.pow(y - e.y, 2));
    }

    public boolean isOutOfBound(int width, int height) {
        return x < 0 || x > width || y < 0 || y > height;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(getColor());
        canvas.drawCircle(x, y, 6, paint);
    }

    public int getDamage() {
        return damage;
    }

    // 添加 getter 方法
    public Enemy getTarget() {
        return target;
    }

    public int getColor() {
        return color;
    }
}