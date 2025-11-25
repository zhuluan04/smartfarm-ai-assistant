package com.linjiu.recognize.api;

import static com.linjiu.recognize.config.HttpUrlConnectConfig.ESP32_URL;

// 存放ESP32的API地址
public class ESP32Api {
    // 显示传感器数据
    private static final String SENSOR_DATA_URL = ESP32_URL + "/sensors/latest";
    // 显示AI识别结果
    private static final String AI_ANALYSIS_URL = ESP32_URL + "/ai/plant-analysis";
}
