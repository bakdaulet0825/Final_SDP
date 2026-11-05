package com.setupbuilder;

public class ComponentFactory {
    public static Component createComponent(String type, String id, String name, double price, String extraParam) {
        switch (type.toUpperCase()) {
            case "CPU":
                return new Component(id, name, "CPU", price, extraParam); // extraParam = socket
            case "MOTHERBOARD":
                return new Component(id, name, "Motherboard", price, extraParam); // extraParam = socket
            case "GPU":
                return new Component(id, name, "GPU", price, "");
            case "RAM":
                return new Component(id, name, "RAM", price, "");
            case "STORAGE":
                return new Component(id, name, "Storage", price, "");
            case "PSU":
                return new Component(id, name, "PowerSupply", price, "");
            default:
                return new Component(id, name, type, price, extraParam);
        }
    }
}
