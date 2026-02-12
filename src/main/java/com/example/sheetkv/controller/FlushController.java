package com.example.sheetkv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sheetkv.model.FlushResult;
import com.example.sheetkv.service.SyncService;

import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/v1")
public class FlushController {
    private final SyncService syncService;

    public FlushController(SyncService syncService) {
        this.syncService = syncService;
    }

    @Operation(summary = "Force reindex")
    @PostMapping("/flush")
    public ResponseEntity<FlushResult> flush() {
        return ResponseEntity.ok(syncService.flush());
    }
}
