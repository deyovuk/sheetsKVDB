package com.example.sheetkv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sheetkv.config.SheetProperties;
import com.example.sheetkv.model.HealthResponse;
import com.example.sheetkv.service.SyncService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/v1")
public class HealthController {
    private final SheetProperties properties;
    private final SyncService syncService;

    public HealthController(SheetProperties properties, SyncService syncService) {
        this.properties = properties;
        this.syncService = syncService;
    }

    @Operation(summary = "Service health")
    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        var response = new HealthResponse("ok", properties.getSpreadsheetId(), syncService.getLastSyncTime());
        return ResponseEntity.ok(response);
    }
}
