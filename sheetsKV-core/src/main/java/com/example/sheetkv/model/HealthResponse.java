package com.example.sheetkv.model;

import java.time.Instant;

public record HealthResponse(String status, String spreadsheetId, Instant lastSyncTime) {
}
