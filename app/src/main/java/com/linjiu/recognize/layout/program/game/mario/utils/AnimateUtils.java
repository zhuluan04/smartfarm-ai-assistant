package com.linjiu.recognize.layout.program.game.mario.utils;

import android.graphics.drawable.AnimationDrawable;

public class AnimateUtils {
    // 获取动画播放时间
    private int getAnimationDuration(AnimationDrawable animation) {
        int duration = 0;
        for (int i = 0; i < animation.getNumberOfFrames(); i++) {
            duration += animation.getDuration(i);
        }
        return duration;
    }
}
