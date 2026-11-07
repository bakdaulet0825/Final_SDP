package com.setupbuilder;

public class ComponentFactory {

    public static Component createComponent(
            String type,
            String id,
            String name,
            double price,
            String extraParam
    ) {
        ComponentCreator creator;

        switch (type.toUpperCase()) {
            case "CPU":
                creator = new CPUCreator();
                break;

            case "MOTHERBOARD":
                creator = new MotherboardCreator();
                break;

            case "GPU":
                creator = new GenericComponentCreator("GPU");
                break;

            case "RAM":
                creator = new GenericComponentCreator("RAM");
                break;

            case "STORAGE":
                creator = new GenericComponentCreator("Storage");
                break;

            case "PSU":
                creator = new GenericComponentCreator("PowerSupply");
                break;

            default:
                creator = new GenericComponentCreator(type);
        }

        return creator.createComponent(
                id,
                name,
                price,
                extraParam
        );
    }
}
