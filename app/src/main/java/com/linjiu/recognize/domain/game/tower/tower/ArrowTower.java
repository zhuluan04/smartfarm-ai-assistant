package com.linjiu.recognize.domain.game.tower.tower;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.linjiu.recognize.domain.game.tower.Projectile;
import com.linjiu.recognize.domain.game.tower.enemy.Enemy;

public class ArrowTower extends Tower {

    public ArrowTower(int x, int y) {
        super(x, y, 10, 0.8f, 200, 50, 30, Color.GREEN); // damage=10, fireRate=0.8s, range=200, cost=50, upgrade=30
    }

    @Override
    public Projectile attack(Enemy target) {
        if (canFire() && target != null) {
            lastFireTime = System.currentTimeMillis();
            return new ArrowProjectile(x, y, target, damage, 12f); // ✅ 正确传参
        }
        return null;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(towerColor);
        canvas.drawCircle(x, y, 30, paint); // 塔身

        // 绘制“A”和等级
        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("A" + level, x, y + 8, paint);
    }

    @Override
    public int getCost() {
        return 50;
    }

    @Override
    public int getUpgradeCost() {
        return 30 + (level - 1) * 20; // 每级升级贵20
    }

    @Override
    public void upgrade() {
        if (canUpgrade()) {
            level++;
            damage += 5;
            range += 30;
        }
    }

    @Override
    public boolean canUpgrade() {
        return level < 3; // 最多升到3级
    }
}