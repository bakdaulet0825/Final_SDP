package com.setupbuilder;

public class GenericComponentCreator extends ComponentCreator {

    private final String type;

    public GenericComponentCreator(String type) {
        this.type = type;
    }

    @Override
    public Component createComponent(
            String id,
            String name,
            double price,
            String extraParam
    ) {
        return new Component(id, name, type, price, extraParam);
    }
}
