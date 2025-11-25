package com.linjiu.recognize.domain.program;

import androidx.fragment.app.Fragment;

// 小程序页面模型
public class MiniProgram {
    private String name;
    private int iconResId;
    private Class<? extends Fragment> targetFragmentClass; // 目标 Fragment 类

    public MiniProgram(String name, int iconResId, Class<? extends Fragment> targetFragmentClass) {
        this.name = name;
        this.iconResId = iconResId;
        this.targetFragmentClass = targetFragmentClass;
    }

    public String getName() {
        return name;
    }

    public int getIconResId() {
        return iconResId;
    }

    public Class<? extends Fragment> getTargetFragmentClass() {
        return targetFragmentClass;
    }
}