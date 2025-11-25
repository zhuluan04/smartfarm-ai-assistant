package com.linjiu.recognize.domain.game.tower.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

public class Enemy {
    public float x, y;
    private int health;
    private int maxHealth;
    private int reward;
    private float speed;
    private List<android.graphics.Point> path;
    private int currentPathIndex = 0;

    public Enemy(List<android.graphics.Point> path, int health, int reward, float speed) {
        this.path = path;
        this.health = health;
        this.maxHealth = health;
        this.reward = reward;
        this.speed = speed;
        this.x = path.get(0).x;
        this.y = path.get(0).y;
    }

    public void update() {
        if (currentPathIndex >= path.size() - 1) return;

        android.graphics.Point target = path.get(currentPathIndex + 1);
        float dx = target.x - x;
        float dy = target.y - y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist < speed) {
            currentPathIndex++;
            if (currentPathIndex >= path.size() - 1) {
                x = target.x;
                y = target.y;
                return;
            }
            target = path.get(currentPathIndex + 1);
            dx = target.x - x;
            dy = target.y - y;
            dist = (float) Math.sqrt(dx * dx + dy * dy);
        }

        x += dx / dist * speed;
        y += dy / dist * speed;
    }

    public void takeDamage(int damage) {
        health -= damage;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public boolean hasReachedEnd() {
        return currentPathIndex >= path.size() - 1;
    }

    public int getReward() {
        return reward;
    }

    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(Color.RED);
        canvas.drawCircle(x, y, 20, paint);
        // 绘制血条
        paint.setColor(Color.GREEN);
        float healthPercent = (float) health / maxHealth;
        canvas.drawRect(x - 20, y - 30, x - 20 + 40 * healthPercent, y - 25, paint);
    }
}