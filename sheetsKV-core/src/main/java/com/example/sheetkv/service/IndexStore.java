package com.example.sheetkv.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class IndexStore {
    private Map<String, Map<String, Integer>> index = new HashMap<>();

    public Integer getRow(String collection, String id) {
        Map<String, Integer> map = index.get(collection);
        return map == null ? null : map.get(id);
    }

    public void put(String collection, String id, int row) {
        index.computeIfAbsent(collection, key -> new HashMap<>()).put(id, row);
    }

    public void remove(String collection, String id) {
        Map<String, Integer> map = index.get(collection);
        if (map != null) {
            map.remove(id);
        }
    }

    public Map<String, Integer> getCollection(String collection) {
        return index.get(collection);
    }

    public Map<String, Map<String, Integer>> getAll() {
        return index;
    }

    public void rebuild(Map<String, Map<String, Integer>> newIndex) {
        this.index = newIndex;
    }

    public void renameCollection(String oldName, String newName) {
        Map<String, Integer> map = index.remove(oldName);
        if (map != null) {
            index.put(newName, map);
        }
    }

    public void removeCollection(String name) {
        index.remove(name);
    }

    public void ensureCollection(String name) {
        index.computeIfAbsent(name, key -> new HashMap<>());
    }

    public void adjustAfterDelete(String collection, int deletedRow) {
        Map<String, Integer> map = index.get(collection);
        if (map == null) {
            return;
        }
        map.replaceAll((id, row) -> row > deletedRow ? row - 1 : row);
    }
}
