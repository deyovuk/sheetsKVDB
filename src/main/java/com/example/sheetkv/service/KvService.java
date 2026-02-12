package com.example.sheetkv.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.sheetkv.adapter.SheetsAdapter;
import com.example.sheetkv.exception.BackendException;
import com.example.sheetkv.exception.NotFoundException;
import com.example.sheetkv.model.BatchDeleteResult;
import com.example.sheetkv.model.BatchGetResult;
import com.example.sheetkv.model.BatchUpsertResult;
import com.example.sheetkv.model.KeyValueEntry;
import com.example.sheetkv.model.PageResult;

@Service
public class KvService {
    private static final Logger logger = LoggerFactory.getLogger(KvService.class);

    private final SheetsAdapter adapter;
    private final IndexStore indexStore;

    public KvService(SheetsAdapter adapter, IndexStore indexStore) {
        this.adapter = adapter;
        this.indexStore = indexStore;
    }

    public String get(String collection, String id) {
        ensureCollectionExists(collection);
        Integer row = indexStore.getRow(collection, id);
        if (row == null) {
            throw new NotFoundException("Key not found");
        }
        String value = adapter.readCell(collection, row);
        if (value == null) {
            throw new BackendException("Value missing for id: " + id);
        }
        return value;
    }

    public void upsert(String collection, String id, String value) {
        ensureCollectionExists(collection);
        Integer row = indexStore.getRow(collection, id);
        if (row == null) {
            int appendedRow = adapter.appendRow(collection, id, value);
            int effectiveRow = appendedRow > 0 ? appendedRow : findRowFromIndex(collection, id);
            indexStore.put(collection, id, effectiveRow);
            logger.info("kv.append collection={} id={} row={}", collection, id, effectiveRow);
        } else {
            adapter.updateCell(collection, row, value);
            logger.info("kv.update collection={} id={} row={}", collection, id, row);
        }
    }

    public void delete(String collection, String id) {
        ensureCollectionExists(collection);
        Integer row = indexStore.getRow(collection, id);
        if (row == null) {
            throw new NotFoundException("Key not found");
        }
        adapter.deleteRow(collection, row);
        indexStore.remove(collection, id);
        indexStore.adjustAfterDelete(collection, row);
        logger.info("kv.delete collection={} id={} row={}", collection, id, row);
    }

    public PageResult<String> listKeys(String collection, int limit, int offset) {
        List<Map.Entry<String, Integer>> ordered = orderedEntries(collection);
        List<String> items = ordered.stream()
                .skip(offset)
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
        String nextCursor = computeNextCursor(offset, limit, ordered.size());
        return new PageResult<>(items, nextCursor);
    }

    public PageResult<KeyValueEntry> listEntries(String collection, int limit, int offset) {
        List<Map.Entry<String, Integer>> ordered = orderedEntries(collection);
        List<KeyValueEntry> items = ordered.stream()
                .skip(offset)
                .limit(limit)
                .map(entry -> new KeyValueEntry(entry.getKey(), adapter.readCell(collection, entry.getValue())))
                .toList();
        String nextCursor = computeNextCursor(offset, limit, ordered.size());
        return new PageResult<>(items, nextCursor);
    }

    public BatchGetResult batchGet(String collection, List<String> ids) {
        ensureCollectionExists(collection);
        List<KeyValueEntry> found = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String id : ids) {
            Integer row = indexStore.getRow(collection, id);
            if (row == null) {
                missing.add(id);
                continue;
            }
            String value = adapter.readCell(collection, row);
            if (value == null) {
                missing.add(id);
                continue;
            }
            found.add(new KeyValueEntry(id, value));
        }
        return new BatchGetResult(found, missing);
    }

    public BatchUpsertResult batchUpsert(String collection, List<KeyValueEntry> items) {
        ensureCollectionExists(collection);
        List<String> created = new ArrayList<>();
        List<String> updated = new ArrayList<>();
        for (KeyValueEntry item : items) {
            Integer row = indexStore.getRow(collection, item.id());
            if (row == null) {
                int appendedRow = adapter.appendRow(collection, item.id(), item.value());
                int effectiveRow = appendedRow > 0 ? appendedRow : findRowFromIndex(collection, item.id());
                indexStore.put(collection, item.id(), effectiveRow);
                created.add(item.id());
            } else {
                adapter.updateCell(collection, row, item.value());
                updated.add(item.id());
            }
        }
        return new BatchUpsertResult(created, updated);
    }

    public BatchDeleteResult batchDelete(String collection, List<String> ids) {
        ensureCollectionExists(collection);
        List<Map.Entry<String, Integer>> rows = ids.stream()
                .map(id -> Map.entry(id, indexStore.getRow(collection, id)))
                .filter(entry -> entry.getValue() != null)
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toList());

        List<String> deleted = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : rows) {
            adapter.deleteRow(collection, entry.getValue());
            indexStore.remove(collection, entry.getKey());
            indexStore.adjustAfterDelete(collection, entry.getValue());
            deleted.add(entry.getKey());
        }
        return new BatchDeleteResult(deleted);
    }

    private List<Map.Entry<String, Integer>> orderedEntries(String collection) {
        Map<String, Integer> map = indexStore.getCollection(collection);
        if (map == null) {
            throw new NotFoundException("Collection not found");
        }
        return map.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .toList();
    }

    private void ensureCollectionExists(String collection) {
        if (indexStore.getCollection(collection) == null) {
            throw new NotFoundException("Collection not found");
        }
    }

    private String computeNextCursor(int offset, int limit, int total) {
        int next = offset + limit;
        return next < total ? String.valueOf(next) : null;
    }

    private int findRowFromIndex(String collection, String id) {
        Integer row = indexStore.getRow(collection, id);
        return row == null ? -1 : row;
    }
}
