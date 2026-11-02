package com.setupbuilder;

public interface SetupService {
    double getCost();
    String getDescription();
}

class BaseSetupService implements SetupService {
    private PCBuild build;

    public BaseSetupService(PCBuild build) {
        this.build = build;
    }

    @Override
    public double getCost() {
        return build.getBaseTotal();
    }

    @Override
    public String getDescription() {
        return "PC Setup: " + build.getName();
    }
}

abstract class ServiceDecorator implements SetupService {
    protected SetupService wrappedService;

    public ServiceDecorator(SetupService service) {
        this.wrappedService = service;
    }

    @Override
    public double getCost() {
        return wrappedService.getCost();
    }

    @Override
    public String getDescription() {
        return wrappedService.getDescription();
    }
}

class ExtendedWarrantyDecorator extends ServiceDecorator {
    public ExtendedWarrantyDecorator(SetupService service) {
        super(service);
    }

    @Override
    public double getCost() {
        return super.getCost() + 49.99;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + 2-Year Extended Warranty ($49.99)";
    }
}

class AssemblyServiceDecorator extends ServiceDecorator {
    public AssemblyServiceDecorator(SetupService service) {
        super(service);
    }

    @Override
    public double getCost() {
        return super.getCost() + 29.99;
    }

    @Override
    public String getDescription() {
        return super.getDescription() + " + Professional Assembly Service ($29.99)";
    }
}
