package com.linjiu.recognize.domain.game.tower.tower;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.linjiu.recognize.domain.game.tower.Projectile;
import com.linjiu.recognize.domain.game.tower.enemy.Enemy;

public class CannonTower extends Tower {

    public CannonTower(int x, int y) {
        super(x, y, 80, 30F, 3, 150, 50, Color.rgb(139, 69, 19)); // 棕色炮塔
        this.range = 200;
        this.fireRate = 1.5f; // 较慢射速，高伤害
    }

    @Override
    public Projectile attack(Enemy target) {
        if (canFire() && target != null) {
            lastFireTime = System.currentTimeMillis();
            return new Projectile(x, y, target, damage, 10f, Color.rgb(255, 69, 0)); // 炮弹橙红色
        }
        return null;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(towerColor);
        canvas.drawCircle(x, y, 30, paint); // 炮塔主体
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x, y, 15, paint); // 炮管/核心
    }

    @Override
    public int getCost() {
        return 80;
    }

    @Override
    public int getUpgradeCost() {
        return 40;
    }

    @Override
    public void upgrade() {
        if (canUpgrade()) {
            level++;
            damage += 15;
            range += 30;
        }
    }

    @Override
    public boolean canUpgrade() {
        return level < 3;
    }
}