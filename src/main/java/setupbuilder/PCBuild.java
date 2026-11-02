package com.setupbuilder;

import java.util.ArrayList;
import java.util.List;

public class PCBuild {
    private String name;
    private List<Component> components;

    public PCBuild(String name) {
        this.name = name;
        this.components = new ArrayList<>();
    }

    public void addComponent(Component c) {
        components.add(c);
    }

    public void removeComponent(Component c) {
        components.remove(c);
    }

    public List<Component> getComponents() {
        return components;
    }

    public String getName() {
        return name;
    }

    public double getBaseTotal() {
        double sum = 0;
        for (Component c : components) {
            sum += c.getPrice();
        }
        return sum;
    }
}
