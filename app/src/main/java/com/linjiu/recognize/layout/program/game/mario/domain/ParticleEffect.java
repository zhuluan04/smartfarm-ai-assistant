package com.linjiu.recognize.layout.program.game.mario.domain;

import android.widget.ImageView;
import android.widget.TextView;

import com.linjiu.recognize.layout.program.game.mario.backup;

public class ParticleEffect {
    public enum Type { DAMAGE_NUMBER, HEAL_EFFECT, EXPLOSION, SPEED_TRAIL }

    public backup.ParticleEffect.Type type;
    public TextView textView;
    public ImageView imageView;
    public float x, y, velocityX, velocityY;
    public long startTime, duration;
    public String text;
    public boolean isText;

    public ParticleEffect(backup.ParticleEffect.Type type, float x, float y, String text, long duration) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.text = text;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.isText = true;
        this.velocityY = -2f;
    }

    public ParticleEffect(backup.ParticleEffect.Type type, float x, float y, long duration) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.duration = duration;
        this.startTime = System.currentTimeMillis();
        this.isText = false;
    }
}