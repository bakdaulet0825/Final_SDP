package com.setupbuilder;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- DEMO: BUILD YOUR SETUP APP ---\n");

        // 1. Factory Method demo
        Component cpu = ComponentFactory.createComponent("CPU", "c1", "AMD Ryzen 7 7800X3D", 389.00, "AM5");
        Component mobo = ComponentFactory.createComponent("MOTHERBOARD", "m1", "B650 Gaming WiFi", 179.99, "AM5");
        Component gpu = ComponentFactory.createComponent("GPU", "g1", "NVIDIA RTX 4070 Super", 599.99, "");
        Component ram = ComponentFactory.createComponent("RAM", "r1", "32GB DDR5 6000MHz", 109.99, "");

        // 2. Builder demo
        PCBuilder builder = new PCBuilder("Gaming Rig 2026");
        PCBuild myBuild = builder.setCPU(cpu)
                .setMotherboard(mobo)
                .setGPU(gpu)
                .setRAM(ram)
                .build();

        // 3. Facade demo
        SetupConfiguratorFacade facade = new SetupConfiguratorFacade();
        facade.validateCompatibility(myBuild);

        // 4. Observer demo
        Component extraStorage = ComponentFactory.createComponent("STORAGE", "s1", "2TB NVMe SSD", 129.99, "");
        facade.addComponentToBuild(myBuild, extraStorage);

        // 5. Strategy demo
        PricingStrategy studentStrategy = new StudentDiscountStrategy();

        // 6. Decorator demo
        SetupService setupService = new BaseSetupService(myBuild);
        setupService = new AssemblyServiceDecorator(setupService);
        setupService = new ExtendedWarrantyDecorator(setupService);

        // Generate report and save
        String summary = facade.generateSummary(myBuild, studentStrategy, setupService);
        System.out.println("\n" + summary);
        facade.saveReportToFile("setup_build_summary.txt", summary);
    }
}
