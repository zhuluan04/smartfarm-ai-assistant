package com.linjiu.recognize;

import android.app.Application;

public class MyApplication extends Application {
    static {
        // 加载 Filament native 库（必须在任何使用前加载）
        System.loadLibrary("filament-jni");
        System.loadLibrary("gltfio-jni");
        // 如果你用到了 filament-utils（如 Skybox、IBL、模型加载工具等），取消注释下一行：
        // System.loadLibrary("filament-utils-jni");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // 注意：Filament ≥ v1.17.0 不再需要 DisplayHelper.init()
        // 所以这里什么都不用做
    }
}