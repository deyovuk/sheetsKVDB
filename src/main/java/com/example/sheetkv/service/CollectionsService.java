package com.example.sheetkv.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.sheetkv.adapter.SheetsAdapter;
import com.example.sheetkv.exception.ConflictException;
import com.example.sheetkv.exception.NotFoundException;

@Service
public class CollectionsService {
    private final SheetsAdapter adapter;
    private final IndexStore indexStore;

    public CollectionsService(SheetsAdapter adapter, IndexStore indexStore) {
        this.adapter = adapter;
        this.indexStore = indexStore;
    }

    public List<String> list() {
        return adapter.listSheets().stream()
                .map(sheet -> sheet.getProperties().getTitle())
                .toList();
    }

    public void create(String name) {
        if (list().contains(name)) {
            throw new ConflictException("Collection already exists");
        }
        adapter.createSheet(name);
        indexStore.ensureCollection(name);
    }

    public void delete(String name) {
        if (!list().contains(name)) {
            throw new NotFoundException("Collection not found");
        }
        adapter.deleteSheet(name);
        indexStore.removeCollection(name);
    }

    public void rename(String oldName, String newName) {
        var names = list();
        if (!names.contains(oldName)) {
            throw new NotFoundException("Collection not found");
        }
        if (names.contains(newName)) {
            throw new ConflictException("Collection already exists");
        }
        adapter.renameSheet(oldName, newName);
        indexStore.renameCollection(oldName, newName);
    }
}
