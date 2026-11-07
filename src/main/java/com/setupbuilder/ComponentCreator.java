package com.setupbuilder;

public abstract class ComponentCreator {

    public abstract Component createComponent(
            String id,
            String name,
            double price,
            String extraParam
    );
}
