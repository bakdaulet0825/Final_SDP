package com.setupbuilder;

import java.io.FileWriter;
import java.io.IOException;

public class SetupConfiguratorFacade {
    private BuildSubject eventManager;

    public SetupConfiguratorFacade() {
        this.eventManager = new BuildSubject();
        this.eventManager.attach(new PriceAlertObserver());
    }

    public boolean validateCompatibility(PCBuild build) {
        Component cpu = null;
        Component mobo = null;

        for (Component c : build.getComponents()) {
            if ("CPU".equalsIgnoreCase(c.getCategory())) cpu = c;
            if ("Motherboard".equalsIgnoreCase(c.getCategory())) mobo = c;
        }

        if (cpu != null && mobo != null) {
            if (!cpu.getSocket().equalsIgnoreCase(mobo.getSocket())) {
                System.err.println("[FACADE VALIDATION ERROR] Socket mismatch! CPU: "
                        + cpu.getSocket() + ", Mobo: " + mobo.getSocket());
                return false;
            }
        }
        System.out.println("[FACADE VALIDATION] All components are fully compatible!");
        return true;
    }

    public void addComponentToBuild(PCBuild build, Component component) {
        build.addComponent(component);
        eventManager.notifyObservers(build, "Added " + component.getName());
    }

    public String generateSummary(PCBuild build, PricingStrategy strategy, SetupService decoratedService) {
        StringBuilder sb = new StringBuilder();
        sb.append("===== SETUP CONFIGURATION REPORT =====\n");
        sb.append("Build Name: ").append(build.getName()).append("\n");
        sb.append("Components:\n");
        for (Component c : build.getComponents()) {
            sb.append(" - ").append(c.toString()).append("\n");
        }
        sb.append("--------------------------------------\n");
        sb.append("Base Parts Cost: $").append(build.getBaseTotal()).append("\n");
        sb.append("Pricing Strategy: ").append(strategy.getStrategyName()).append("\n");
        double strategyPrice = strategy.calculatePrice(build.getBaseTotal());
        sb.append("Calculated Total (Parts): $").append(String.format("%.2f", strategyPrice)).append("\n");
        sb.append("Services & Extras: ").append(decoratedService.getDescription()).append("\n");
        sb.append("FINAL GRAND TOTAL: $").append(String.format("%.2f", decoratedService.getCost())).append("\n");
        sb.append("======================================");
        return sb.toString();
    }

    public void saveReportToFile(String filename, String content) {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(content);
            System.out.println("[FACADE STORAGE] Configuration saved successfully to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving file: " + e.getMessage());
        }
    }
}
