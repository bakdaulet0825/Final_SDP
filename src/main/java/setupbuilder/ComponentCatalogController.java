
package com.setupbuilder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ComponentCatalogController {

    public record ComponentOption(
            String id,
            String name,
            double price,
            String socket
    ) {}

    @GetMapping("/api/components")
    public Map<String, List<ComponentOption>> getComponents() {
        return Map.of(
                "cpu", List.of(
                        new ComponentOption("cpu1", "Intel Core i5-12400F", 150, "LGA1700"),
                        new ComponentOption("cpu2", "Intel Core i7-12700K", 280, "LGA1700"),
                        new ComponentOption("cpu3", "AMD Ryzen 5 5600", 130, "AM4")
                ),
                "gpu", List.of(
                        new ComponentOption("gpu1", "NVIDIA RTX 3050", 220, null),
                        new ComponentOption("gpu2", "NVIDIA RTX 4060", 320, null),
                        new ComponentOption("gpu3", "AMD RX 7600", 270, null)
                ),
                "motherboard", List.of(
                        new ComponentOption("mb1", "ASUS PRIME B660M", 130, "LGA1700"),
                        new ComponentOption("mb2", "MSI B550M PRO", 110, "AM4")
                ),
                "ram", List.of(
                        new ComponentOption("ram1", "16GB DDR4", 50, null),
                        new ComponentOption("ram2", "32GB DDR4", 90, null),
                        new ComponentOption("ram3", "32GB DDR5", 130, null)
                ),
                "storage", List.of(
                        new ComponentOption("ssd1", "512GB NVMe SSD", 45, null),
                        new ComponentOption("ssd2", "1TB NVMe SSD", 80, null),
                        new ComponentOption("ssd3", "2TB NVMe SSD", 150, null)
                )
        );
    }
}
