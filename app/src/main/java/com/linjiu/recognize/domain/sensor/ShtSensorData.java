package com.linjiu.recognize.domain.sensor;

public class ShtSensorData {

    private String deviceId;

    private Double temperature;

    private Double humidity;

    private String recordedAt; // 或 LocalDateTime（但 Gson 默认不支持，建议用 String）

    // Getters and Setters
    public String getDeviceId() { return deviceId; }

    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }

    public Double getTemperature() { return temperature; }

    public void setTemperature(Double temperature) { this.temperature = temperature; }

    public Double getHumidity() { return humidity; }

    public void setHumidity(Double humidity) { this.humidity = humidity; }

    public String getRecordedAt() { return recordedAt; }

    public void setRecordedAt(String recordedAt) { this.recordedAt = recordedAt; }
}