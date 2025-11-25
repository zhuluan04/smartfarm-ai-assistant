package com.linjiu.recognize.domain.plant;

/**
 * 植物类型主类
 */
public class Plant {
    private String name;
    private int imageResId;

    public Plant(String name, int imageResId) {
        this.name = name;
        this.imageResId = imageResId;
    }

    public String getName() {
        return name;
    }

    public int getImageResId() {
        return imageResId;
    }
}
