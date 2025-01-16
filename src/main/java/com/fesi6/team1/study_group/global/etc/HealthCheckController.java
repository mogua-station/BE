package com.fesi6.team1.study_group.global.etc;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {

    @GetMapping("/home/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("healthy");
    }
}

