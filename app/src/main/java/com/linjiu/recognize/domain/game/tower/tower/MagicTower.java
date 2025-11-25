package com.linjiu.recognize.domain.game.tower.tower;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.linjiu.recognize.domain.game.tower.Projectile;
import com.linjiu.recognize.domain.game.tower.enemy.Enemy;

public class MagicTower extends Tower {

    public MagicTower(int x, int y) {
        super(x, y, 60, 5, 3, 250, 100, Color.rgb(128, 0, 128)); // 紫色魔法塔
        this.range = 300; // 超远距离
        this.fireRate = 3.0f; // 最慢射速，范围伤害
    }

    @Override
    public Projectile attack(Enemy target) {
        if (canFire() && target != null) {
            lastFireTime = System.currentTimeMillis();
            // 魔法弹：范围伤害，减速效果可后续扩展
            return new Projectile(x, y, target, damage, 8f, Color.CYAN);
        }
        return null;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        paint.setColor(towerColor);
        canvas.drawCircle(x, y, 25, paint); // 魔法塔主体
        paint.setColor(Color.WHITE);
        canvas.drawText("★", x - 10, y + 5, paint); // 魔法符号
    }

    @Override
    public int getCost() {
        return 120;
    }

    @Override
    public int getUpgradeCost() {
        return 60;
    }

    @Override
    public void upgrade() {
        if (canUpgrade()) {
            level++;
            damage += 10;
            range += 50;
        }
    }

    @Override
    public boolean canUpgrade() {
        return level < 3;
    }
}