package com.linjiu.recognize.domain.sensor;

public class SensorItem {
    private int id;
    private String name;
    private int iconRes;
    private String value;
    private String unit;
    private Status status;

    public enum Status {
        ONLINE, OFFLINE, WARNING
    }

    public SensorItem(int id, String name, int iconRes, String value, String unit) {
        this(id, name, iconRes, value, unit, Status.ONLINE);
    }

    public SensorItem(int id, String name, int iconRes, String value, String unit, Status status) {
        this.id = id;
        this.name = name;
        this.iconRes = iconRes;
        this.value = value;
        this.unit = unit;
        this.status = status;
    }

    // Getters
    public int getId() { return id; }
    public String getName() { return name; }
    public int getIconRes() { return iconRes; }
    public String getValue() { return value; }
    public String getUnit() { return unit; }
    public Status getStatus() { return status; }

    // Setters
    public void setValue(String value) { this.value = value; }
    public void setStatus(Status status) { this.status = status; }
}