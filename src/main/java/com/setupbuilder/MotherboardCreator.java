package com.setupbuilder;

public class MotherboardCreator extends ComponentCreator {

    @Override
    public Component createComponent(
            String id,
            String name,
            double price,
            String extraParam
    ) {
        return new Component(id, name, "Motherboard", price, extraParam);
    }
}
