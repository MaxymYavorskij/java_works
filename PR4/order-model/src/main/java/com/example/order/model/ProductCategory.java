package com.example.order.model;

public enum ProductCategory {
    ELECTRONICS("Електроніка"),
    CLOTHING("Одяг");

    private final String title;

    ProductCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
