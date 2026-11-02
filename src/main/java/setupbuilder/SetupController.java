
package com.setupbuilder;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SetupController {

    @GetMapping("/api/status")
    public Map<String, String> getStatus() {
        return Map.of(
                "application", "Build Your Setup",
                "status", "Running",
                "message", "Backend is working!"
        );
    }
}
