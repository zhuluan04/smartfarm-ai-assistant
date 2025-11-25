package com.linjiu.recognize.layout.program.game.mario.domain;

import android.widget.ImageView;

import com.linjiu.recognize.layout.program.game.mario.backup;

public class PowerUp {

    public backup.PowerUp.Type type;
    public ImageView imageView;
    public float x, y;
    public long spawnTime;
    public boolean collected = false;

    public PowerUp(backup.PowerUp.Type type, ImageView imageView, float x, float y) {
        this.type = type;
        this.imageView = imageView;
        this.x = x;
        this.y = y;
        this.spawnTime = System.currentTimeMillis();
    }
}
