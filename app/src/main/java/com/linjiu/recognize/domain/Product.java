package com.linjiu.recognize.domain;

public class Product {
    private String title;
    private double price;
    private String description;

    public Product(String title, double price, String description) {
        this.title = title;
        this.price = price;
        this.description = description;
    }

    // Getters
    public String getTitle() { return title; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
}