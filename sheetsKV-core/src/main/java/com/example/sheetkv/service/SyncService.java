package com.example.sheetkv.service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.sheetkv.adapter.SheetsAdapter;
import com.example.sheetkv.model.FlushResult;

@Service
public class SyncService {
    private static final Logger logger = LoggerFactory.getLogger(SyncService.class);

    private final SheetsAdapter adapter;
    private final IndexStore indexStore;
    private Instant lastSyncTime;

    public SyncService(SheetsAdapter adapter, IndexStore indexStore) {
        this.adapter = adapter;
        this.indexStore = indexStore;
    }

    public FlushResult flush() {
        var sheets = adapter.listSheets();
        Map<String, Map<String, Integer>> newIndex = new HashMap<>();
        Map<String, List<String>> duplicates = new HashMap<>();
        int totalKeys = 0;

        sheets.forEach(sheet -> {
            String name = sheet.getProperties().getTitle();
            List<String> ids = adapter.readColumnA(name);
            Map<String, Integer> map = new HashMap<>();
            Map<String, Integer> duplicateCounts = new HashMap<>();

            for (int i = 0; i < ids.size(); i++) {
                String id = ids.get(i);
                int row = i + 1;
                if (id == null || id.isBlank()) {
                    continue;
                }
                if (!map.containsKey(id)) {
                    map.put(id, row);
                } else {
                    duplicateCounts.put(id, duplicateCounts.getOrDefault(id, 1) + 1);
                }
            }

            if (!duplicateCounts.isEmpty()) {
                duplicates.put(name, duplicateCounts.keySet().stream().toList());
                logger.warn("flush.duplicates sheet={} ids={}", name, duplicateCounts.keySet());
            }

            newIndex.put(name, map);
        });

        totalKeys = newIndex.values().stream().mapToInt(Map::size).sum();
        indexStore.rebuild(newIndex);
        lastSyncTime = Instant.now();

        return new FlushResult(sheets.size(), totalKeys, duplicates, lastSyncTime);
    }

    public Instant getLastSyncTime() {
        return lastSyncTime;
    }
}
