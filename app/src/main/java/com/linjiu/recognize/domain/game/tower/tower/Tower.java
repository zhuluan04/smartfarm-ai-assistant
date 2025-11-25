package com.linjiu.recognize.domain.game.tower.tower;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.linjiu.recognize.domain.game.tower.Projectile;
import com.linjiu.recognize.domain.game.tower.enemy.Enemy;

import java.util.List;

// 塔基类
public abstract class Tower {

    protected int x, y;

    protected int damage;

    protected float fireRate; // 秒

    protected int range;

    protected long lastFireTime;

    protected int level = 1;

    protected int cost;

    protected int upgradeCost;

    protected int towerColor;

    /**
     * 塔构造函数
     * @param x 初始x坐标
     * @param y 初始y坐标
     * @param damage 伤害
     * @param fireRate 发射频率
     * @param range 范围
     * @param cost 成本
     * @param upgradeCost 升级成本
     * @param color 颜色
     */
    public Tower(int x, int y, int damage, float fireRate, int range, int cost, int upgradeCost, int color) {
        this.x = x;
        this.y = y;
        this.damage = damage;
        this.fireRate = fireRate;
        this.range = range;
        this.cost = cost;
        this.upgradeCost = upgradeCost;
        this.towerColor = color;
    }


    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    // 更新，如果找到目标则打击
    public void update(List<Enemy> enemies) {
        Enemy target = findTarget(enemies);
        if (target != null && canFire()) {
            Projectile projectile = attack(target);
            if (projectile != null) {
                // GameSurfaceView 会收集这个 projectile
            }
        }
    }

    // 寻找目标
    protected Enemy findTarget(List<Enemy> enemies) {
        for (Enemy e : enemies) {
            if (distanceTo(e) <= range) {
                return e;
            }
        }
        return null;
    }

    // 计算距离
    protected float distanceTo(Enemy e) {
        return (float) Math.sqrt(Math.pow(x - e.x, 2) + Math.pow(y - e.y, 2));
    }

    // 是否可以攻击
    protected boolean canFire() {
        return System.currentTimeMillis() - lastFireTime > fireRate * 1000;
    }

    public abstract Projectile attack(Enemy target);

    public abstract void draw(Canvas canvas, Paint paint);

    public abstract int getCost();

    public abstract int getUpgradeCost();

    public abstract void upgrade();

    public abstract boolean canUpgrade();

    public boolean contains(float touchX, float touchY) {
        return Math.hypot(touchX - x, touchY - y) < 40;
    }

    public int getLevel() { return level; }
}