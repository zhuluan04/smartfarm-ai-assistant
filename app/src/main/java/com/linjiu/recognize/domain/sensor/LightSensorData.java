package com.linjiu.recognize.domain.sensor;

/**
 * 光照传感器数据
 */
public class LightSensorData {

    private String deviceId;

    private Float light; // 单位：Lux

    private String recordedAt;

    // Getters and Setters
    public String getDeviceId() { return deviceId; }

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Float getLight() { return light; }

    public void setLight(Float light) { this.light = light; }

    public String getRecordedAt() { return recordedAt; }

    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }
}