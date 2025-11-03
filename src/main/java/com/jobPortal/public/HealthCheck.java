package com.jobPortal.HealthCheck;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/com/jobPortal/public")
public class HealthCheck {


    @GetMapping("/health-check")
    public String healthCheck() {
        return "Health check OK";
    }
}
