package com.linjiu.recognize.layout.program.game.mario.domain;

import android.widget.ImageView;

import com.linjiu.recognize.layout.program.game.mario.backup;

public class Obstacle {
    public enum Type { ROCK, TREE, WALL }

    public backup.Obstacle.Type type;
    public ImageView imageView;
    public float x, y, width, height;
    public boolean destructible;
    public int health;

    public Obstacle(backup.Obstacle.Type type, ImageView imageView, float x, float y, float width, float height) {
        this.type = type;
        this.imageView = imageView;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.destructible = (type != backup.Obstacle.Type.WALL);
        this.health = destructible ? 30 : Integer.MAX_VALUE;
    }
}