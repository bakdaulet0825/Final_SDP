
package com.setupbuilder;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
            String servicesDescription
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

        PCBuild pcBuild = new PCBuilder("My Custom PC")
                .setCPU(cpu)
                .setMotherboard(motherboard)
                .setGPU(gpu)
                .setRAM(ram)
                .addStorage(storage)
                .build();

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
                service.getDescription()
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
