package com.setupbuilder;

public class PCBuilder {
    private PCBuild build;

    public PCBuilder(String buildName) {
        this.build = new PCBuild(buildName);
    }

    public PCBuilder setCPU(Component cpu) {
        build.addComponent(cpu);
        return this;
    }

    public PCBuilder setMotherboard(Component mobo) {
        build.addComponent(mobo);
        return this;
    }

    public PCBuilder setGPU(Component gpu) {
        build.addComponent(gpu);
        return this;
    }

    public PCBuilder setRAM(Component ram) {
        build.addComponent(ram);
        return this;
    }

    public PCBuilder addStorage(Component storage) {
        build.addComponent(storage);
        return this;
    }

    public PCBuilder setPSU(Component psu) {
        build.addComponent(psu);
        return this;
    }

    public PCBuild build() {
        return this.build;
    }
}
