package com.linjiu.recognize.domain.sensor;

/**
 * Soil Sensor Data
 */
public class SoilSensorData {

    private String deviceId;

    private Float soil; // 单位：%

    private String recordedAt;


    // Getters and Setters
    public String getDeviceId() { return deviceId; }

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Float getSoil() { return soil; }

    public void setSoil(Float soil) { this.soil = soil; }

    public String getRecordedAt() { return recordedAt; }

    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }
}