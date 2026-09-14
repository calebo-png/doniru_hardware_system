package com.example.projecthadwareinventorysystem;

public class InventoryItem {
    private String name;
    private String model;
    private String serial;
    private String status;

    public InventoryItem(String name, String model, String serial, String status) {
        this.name = name;
        this.model = model;
        this.serial = serial;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String getSerial() {
        return serial;
    }

    public String getStatus() {
        return status;
    }
}
