package com.linjiu.recognize.domain.game.tower.tower;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.linjiu.recognize.domain.game.tower.Projectile;
import com.linjiu.recognize.domain.game.tower.enemy.Enemy;

public class ArrowProjectile extends Projectile {

    private Path arrowPath = new Path();

    // ✅ 修正构造函数：匹配父类参数
    public ArrowProjectile(float startX, float startY, Enemy target, int damage, float speed) {
        super(startX, startY, target, damage, speed, Color.CYAN); // 青色箭矢
        // ✅ 不设置 speedX/Y —— 父类自动计算方向
    }

    // ✅ 只重写 draw()，绘制动态箭头
    @Override
    public void draw(Canvas canvas, Paint paint) {
        Enemy target = getTarget(); // 使用 getter 方法获取 target
        if (target == null || target.isDead()) return;

        // 计算飞行方向（单位向量）
        float dx = target.x - x;
        float dy = target.y - y;
        double len = Math.sqrt(dx * dx + dy * dy);
        if (len < 1e-6) return; // 避免除零

        float ux = (float) (dx / len);
        float uy = (float) (dy / len);

        // 设置画笔
        paint.setColor(getColor()); // 使用 getter 方法获取 color
        paint.setStrokeWidth(3f);
        paint.setStyle(Paint.Style.STROKE);

        // 构建箭头路径
        arrowPath.reset();

        // 箭身（从尾部到头部）
        float bodyLength = 15f;
        float tailX = x - ux * bodyLength;
        float tailY = y - uy * bodyLength;
        arrowPath.moveTo(tailX, tailY);
        arrowPath.lineTo(x, y);

        // 箭头（V形）
        float wingLength = 8f;
        float perpX = -uy; // 垂直方向
        float perpY = ux;

        arrowPath.moveTo(x, y);
        arrowPath.lineTo(x - ux * 10 + perpX * wingLength, y - uy * 10 + perpY * wingLength);

        arrowPath.moveTo(x, y);
        arrowPath.lineTo(x - ux * 10 - perpX * wingLength, y - uy * 10 - perpY * wingLength);

        // 绘制
        canvas.drawPath(arrowPath, paint);
    }
}