package com.example.sheetkv.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record FlushResult(int collections, int totalKeys, Map<String, List<String>> duplicates, Instant syncedAt) {
}
