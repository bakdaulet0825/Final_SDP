package com.setupbuilder;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class BuildController {

    private final ComponentCatalogController catalog;
    private final SetupConfiguratorFacade facade;

    public BuildController(ComponentCatalogController catalog) {
        this.catalog = catalog;
        this.facade = new SetupConfiguratorFacade();
    }

    public record BuildRequest(
            String cpu,
            String gpu,
            String motherboard,
            String ram,
            String storage,
            String pricingStrategy,
            boolean assembly,
            boolean extendedWarranty
    ) {}

    public record BuildResponse(
            String buildName,
            double baseTotal,
            double total,
            double monthlyPayment,
            boolean compatible,
            String message,
            String strategyName,
            int componentCount,
            double servicesCost,
            String servicesDescription,
            List<String> notifications
    ) {}

    @PostMapping("/build")
    public BuildResponse build(@RequestBody BuildRequest request) {

        Map<String, List<ComponentCatalogController.ComponentOption>> catalogData =
                catalog.getComponents();

        Component cpu = createComponent(catalogData, "cpu", request.cpu());
        Component gpu = createComponent(catalogData, "gpu", request.gpu());
        Component motherboard = createComponent(
                catalogData, "motherboard", request.motherboard()
        );
        Component ram = createComponent(catalogData, "ram", request.ram());
        Component storage = createComponent(
                catalogData, "storage", request.storage()
        );

        PCBuild pcBuild = new PCBuilder("My Custom PC").build();

        List<String> notifications = new ArrayList<>();

        BuildSubject subject = new BuildSubject();

        subject.attach(new PriceAlertObserver());

        subject.attach((build, changeMessage) -> {
            notifications.add(
                    "Build updated: " + changeMessage
            );

            if (build.getBaseTotal() > 1000) {
                notifications.add(
                        "Price alert: Base component cost exceeds $1000!"
                );
            }
        });

        addComponent(pcBuild, cpu, subject);
        addComponent(pcBuild, motherboard, subject);
        addComponent(pcBuild, gpu, subject);
        addComponent(pcBuild, ram, subject);
        addComponent(pcBuild, storage, subject);

        boolean compatible = facade.validateCompatibility(pcBuild);

        double baseTotal = pcBuild.getBaseTotal();

        String selectedStrategy = request.pricingStrategy() == null
                ? "standard"
                : request.pricingStrategy();

        PricingStrategy strategy;
        int months = 0;

        switch (selectedStrategy) {
            case "student":
                strategy = new StudentDiscountStrategy();
                break;
            case "installment6":
                months = 6;
                strategy = new InstallmentPlanStrategy(months);
                break;
            case "installment12":
                months = 12;
                strategy = new InstallmentPlanStrategy(months);
                break;
            case "standard":
                strategy = new StandardPricingStrategy();
                break;
            default:
                throw new InvalidComponentException(
                        "Invalid pricing strategy: " + selectedStrategy
                );
        }

        double partsTotal;

        if (months > 0) {
            double monthlyPartsPayment =
                    strategy.calculatePrice(baseTotal);

            partsTotal = monthlyPartsPayment * months;
        } else {
            partsTotal = strategy.calculatePrice(baseTotal);
        }

        SetupService service = new BaseSetupService(pcBuild);

        if (request.assembly()) {
            service = new AssemblyServiceDecorator(service);
        }

        if (request.extendedWarranty()) {
            service = new ExtendedWarrantyDecorator(service);
        }

        double servicesCost = service.getCost() - baseTotal;
        double total = partsTotal + servicesCost;

        double monthlyPayment = months > 0
                ? total / months
                : 0;

        if (request.assembly()) {
            notifications.add("Service selected: Professional PC Assembly");
        }

        if (request.extendedWarranty()) {
            notifications.add("Service selected: Extended Warranty");
        }

        notifications.add(
                compatible
                        ? "Compatibility check passed."
                        : "Compatibility warning: CPU and motherboard sockets do not match."
        );

        notifications.add(
                "Pricing strategy applied: " + strategy.getStrategyName()
        );

        String message = compatible
                ? "CPU and motherboard are compatible!"
                : "Incompatible components: CPU and motherboard sockets do not match!";

        return new BuildResponse(
                pcBuild.getName(),
                round(baseTotal),
                round(total),
                round(monthlyPayment),
                compatible,
                message,
                strategy.getStrategyName(),
                pcBuild.getComponents().size(),
                round(servicesCost),
                service.getDescription(),
                notifications
        );
    }

    private void addComponent(
            PCBuild build,
            Component component,
            BuildSubject subject
    ) {
        build.addComponent(component);
        subject.notifyObservers(
                build,
                component.getName() + " selected"
        );
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private Component createComponent(
            Map<String, List<ComponentCatalogController.ComponentOption>> catalogData,
            String category,
            String id
    ) {
        ComponentCatalogController.ComponentOption option =
                catalogData.get(category).stream()
                        .filter(component -> component.id().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new InvalidComponentException(
                                "Invalid component: " + category + " / " + id
                        ));

        return ComponentFactory.createComponent(
                category,
                option.id(),
                option.name(),
                option.price(),
                option.socket() == null ? "" : option.socket()
        );
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    private static class InvalidComponentException extends RuntimeException {
        InvalidComponentException(String message) {
            super(message);
        }
    }
}
