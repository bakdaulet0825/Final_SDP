package com.setupbuilder;

public class CPUCreator extends ComponentCreator {

    @Override
    public Component createComponent(
            String id,
            String name,
            double price,
            String extraParam
    ) {
        return new Component(id, name, "CPU", price, extraParam);
    }
}
