package com.setupbuilder;

public class Component {
    private String id;
    private String name;
    private String category; // CPU, GPU, RAM, Storage, PowerSupply, Motherboard
    private double price;
    private String socket; // Compatibility field (e.g. AM5, LGA1700)

    public Component(String id, String name, String category, double price, String socket) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.socket = socket;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public String getSocket() { return socket; }

    @Override
    public String toString() {
        return name + " (" + category + ") - $" + price + (socket != null && !socket.isEmpty() ? " [" + socket + "]" : "");
    }
}
