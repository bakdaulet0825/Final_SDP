package com.setupbuilder;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ReportController {

    private final ComponentCatalogController catalog;
    private final SetupConfiguratorFacade facade;

    public ReportController(ComponentCatalogController catalog) {
        this.catalog = catalog;
        this.facade = new SetupConfiguratorFacade();
    }

    public record ReportRequest(
            String name,
            String cpu,
            String gpu,
            String motherboard,
            String ram,
            String storage,
            String pricingStrategy,
            boolean assembly,
            boolean extendedWarranty
    ) {}

    @PostMapping("/export-report")
    public ResponseEntity<byte[]> exportReport(
            @RequestBody ReportRequest request
    ) {
        Map<String, List<ComponentCatalogController.ComponentOption>> data =
                catalog.getComponents();

        Component cpu = findComponent(data, "cpu", request.cpu());
        Component gpu = findComponent(data, "gpu", request.gpu());
        Component motherboard = findComponent(
                data, "motherboard", request.motherboard()
        );
        Component ram = findComponent(data, "ram", request.ram());
        Component storage = findComponent(data, "storage", request.storage());

        String buildName = request.name() == null
                || request.name().isBlank()
                ? "My Custom PC"
                : request.name().trim();

        PCBuild build = new PCBuilder(buildName)
                .setCPU(cpu)
                .setMotherboard(motherboard)
                .setGPU(gpu)
                .setRAM(ram)
                .addStorage(storage)
                .build();

        boolean compatible = facade.validateCompatibility(build);

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
                throw new IllegalArgumentException(
                        "Invalid pricing strategy: " + selectedStrategy
                );
        }

        double baseTotal = build.getBaseTotal();

        double partsTotal = months > 0
                ? strategy.calculatePrice(baseTotal) * months
                : strategy.calculatePrice(baseTotal);

        SetupService service = new BaseSetupService(build);

        if (request.assembly()) {
            service = new AssemblyServiceDecorator(service);
        }

        if (request.extendedWarranty()) {
            service = new ExtendedWarrantyDecorator(service);
        }

        double servicesCost = service.getCost() - baseTotal;
        double finalTotal = partsTotal + servicesCost;
        double monthlyPayment = months > 0 ? finalTotal / months : 0;

        StringBuilder report = new StringBuilder();

        report.append("========================================\n");
        report.append("          BUILD YOUR SETUP REPORT\n");
        report.append("========================================\n\n");

        report.append("Build Name: ").append(buildName).append("\n");
        report.append("Generated: ").append(
                LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                )
        ).append("\n\n");

        report.append("SELECTED COMPONENTS\n");
        report.append("----------------------------------------\n");

        for (Component component : build.getComponents()) {
            report.append(component.getCategory())
                    .append(": ")
                    .append(component.getName())
                    .append(" - ")
                    .append(money(component.getPrice()))
                    .append("\n");
        }

        report.append("\nPRICING DETAILS\n");
        report.append("----------------------------------------\n");
        report.append("Base Parts Cost: ")
                .append(money(baseTotal)).append("\n");

        report.append("Pricing Strategy: ")
                .append(strategy.getStrategyName()).append("\n");

        report.append("Parts Total After Strategy: ")
                .append(money(partsTotal)).append("\n");

        report.append("Assembly Service: ")
                .append(request.assembly() ? "$29.99" : "$0.00")
                .append("\n");

        report.append("Extended Warranty: ")
                .append(request.extendedWarranty() ? "$49.99" : "$0.00")
                .append("\n");

        report.append("Additional Services Total: ")
                .append(money(servicesCost)).append("\n");

        report.append("\nFINAL TOTAL: ")
                .append(money(finalTotal)).append("\n");

        if (months > 0) {
            report.append("Installment Period: ")
                    .append(months).append(" months\n");

            report.append("Monthly Payment: ")
                    .append(money(monthlyPayment)).append("\n");
        }

        report.append("\nCOMPATIBILITY CHECK\n");
        report.append("----------------------------------------\n");
        report.append(compatible
                ? "PASS - CPU and motherboard are compatible.\n"
                : "FAIL - CPU and motherboard sockets do not match.\n");

        report.append("\n========================================\n");
        report.append("          END OF REPORT\n");
        report.append("========================================\n");

        byte[] bytes = report.toString()
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"build-report.txt\""
                )
                .contentType(
                        new MediaType("text", "plain", StandardCharsets.UTF_8)
                )
                .body(bytes);
    }

    private Component findComponent(
            Map<String, List<ComponentCatalogController.ComponentOption>> data,
            String category,
            String id
    ) {
        ComponentCatalogController.ComponentOption option =
                data.get(category).stream()
                        .filter(item -> item.id().equals(id))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException(
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

    private String money(double value) {
        return String.format(java.util.Locale.US, "$%.2f", value);
    }
}
