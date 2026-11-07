package com.setupbuilder;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuildPatternsTest {

    private Component component(
            String id,
            String name,
            String type,
            double price,
            String socket
    ) {
        return ComponentFactory.createComponent(
                type, id, name, price, socket
        );
    }

    private PCBuild createCompatibleBuild() {
        return new PCBuilder("Gaming PC")
                .setCPU(component(
                        "cpu1", "Intel Core i5-12400F",
                        "CPU", 150, "LGA1700"
                ))
                .setMotherboard(component(
                        "mb1", "ASUS PRIME B660M",
                        "MOTHERBOARD", 130, "LGA1700"
                ))
                .setGPU(component(
                        "gpu1", "RTX 3050",
                        "GPU", 220, ""
                ))
                .setRAM(component(
                        "ram1", "16GB DDR4",
                        "RAM", 50, ""
                ))
                .addStorage(component(
                        "ssd1", "512GB SSD",
                        "STORAGE", 45, ""
                ))
                .build();
    }

    @Test
    void builderCreatesCorrectBuild() {
        PCBuild build = createCompatibleBuild();

        assertEquals("Gaming PC", build.getName());
        assertEquals(5, build.getComponents().size());
        assertEquals(595.0, build.getBaseTotal(), 0.001);
    }

    @Test
    void factoryCreatesCPU() {
        Component cpu = component(
                "cpu1", "Intel Core i5",
                "CPU", 150, "LGA1700"
        );

        assertEquals("CPU", cpu.getCategory());
        assertEquals("LGA1700", cpu.getSocket());
        assertEquals(150.0, cpu.getPrice(), 0.001);
    }

    @Test
    void factoryMethodCreatesCorrectComponents() {
        ComponentCreator cpuCreator = new CPUCreator();
        ComponentCreator motherboardCreator = new MotherboardCreator();

        Component cpu = cpuCreator.createComponent(
                "cpu1",
                "Intel Core i5-12400F",
                150.0,
                "LGA1700"
        );

        Component motherboard = motherboardCreator.createComponent(
                "mb1",
                "ASUS PRIME B660M",
                130.0,
                "LGA1700"
        );

        assertEquals("CPU", cpu.getCategory());
        assertEquals("Motherboard", motherboard.getCategory());

        assertEquals(150.0, cpu.getPrice(), 0.001);
        assertEquals(130.0, motherboard.getPrice(), 0.001);

        assertEquals("LGA1700", cpu.getSocket());
        assertEquals("LGA1700", motherboard.getSocket());
    }

    @Test
    void standardPricingReturnsBasePrice() {
        PricingStrategy strategy = new StandardPricingStrategy();

        assertEquals(
                1000.0,
                strategy.calculatePrice(1000),
                0.001
        );
    }

    @Test
    void studentDiscountAppliesTenPercent() {
        PricingStrategy strategy = new StudentDiscountStrategy();

        assertEquals(
                900.0,
                strategy.calculatePrice(1000),
                0.001
        );
    }

    @Test
    void sixMonthInstallmentCalculatesPayment() {
        PricingStrategy strategy = new InstallmentPlanStrategy(6);

        assertEquals(
                175.0,
                strategy.calculatePrice(1000),
                0.001
        );
    }

    @Test
    void twelveMonthInstallmentCalculatesPayment() {
        PricingStrategy strategy = new InstallmentPlanStrategy(12);

        assertEquals(
                1120.0 / 12.0,
                strategy.calculatePrice(1000),
                0.001
        );
    }

    @Test
    void decoratorAddsAssemblyCost() {
        PCBuild build = createCompatibleBuild();

        SetupService service = new AssemblyServiceDecorator(
                new BaseSetupService(build)
        );

        assertEquals(624.99, service.getCost(), 0.001);
    }

    @Test
    void decoratorAddsBothServices() {
        PCBuild build = createCompatibleBuild();

        SetupService service = new ExtendedWarrantyDecorator(
                new AssemblyServiceDecorator(
                        new BaseSetupService(build)
                )
        );

        assertEquals(674.98, service.getCost(), 0.001);
    }

    @Test
    void facadeAcceptsCompatibleComponents() {
        SetupConfiguratorFacade facade =
                new SetupConfiguratorFacade();

        assertTrue(
                facade.validateCompatibility(
                        createCompatibleBuild()
                )
        );
    }

    @Test
    void facadeRejectsSocketMismatch() {
        PCBuild build = new PCBuilder("Invalid PC")
                .setCPU(component(
                        "cpu3", "AMD Ryzen 5 5600",
                        "CPU", 130, "AM4"
                ))
                .setMotherboard(component(
                        "mb1", "ASUS PRIME B660M",
                        "MOTHERBOARD", 130, "LGA1700"
                ))
                .build();

        SetupConfiguratorFacade facade =
                new SetupConfiguratorFacade();

        assertFalse(facade.validateCompatibility(build));
    }

    @Test
    void observerReceivesBuildUpdates() {
        PCBuild build = new PCBuilder("Observer Test").build();

        BuildSubject subject = new BuildSubject();
        List<String> notifications = new ArrayList<>();

        subject.attach((updatedBuild, message) ->
                notifications.add(message)
        );

        Component cpu = component(
                "cpu1", "Intel Core i5",
                "CPU", 150, "LGA1700"
        );

        build.addComponent(cpu);
        subject.notifyObservers(build, "CPU added");

        assertEquals(1, notifications.size());
        assertEquals("CPU added", notifications.get(0));
    }

    @Test
    void observerCanBeDetached() {
        PCBuild build = new PCBuilder("Observer Test").build();

        BuildSubject subject = new BuildSubject();
        List<String> notifications = new ArrayList<>();

        BuildObserver observer = (updatedBuild, message) ->
                notifications.add(message);

        subject.attach(observer);
        subject.detach(observer);
        subject.notifyObservers(build, "Updated");

        assertTrue(notifications.isEmpty());
    }
}
