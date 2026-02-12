package com.example.sheetkv.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sheetkv.model.CreateCollectionRequest;
import com.example.sheetkv.model.RenameCollectionRequest;
import com.example.sheetkv.service.CollectionsService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/collections")
public class CollectionsController {
    private final CollectionsService collectionsService;

    public CollectionsController(CollectionsService collectionsService) {
        this.collectionsService = collectionsService;
    }

    @Operation(summary = "List collections")
    @GetMapping
    public ResponseEntity<List<String>> list() {
        return ResponseEntity.ok(collectionsService.list());
    }

    @Operation(summary = "Create collection")
    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreateCollectionRequest request) {
        collectionsService.create(request.name());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete collection")
    @DeleteMapping("/{collection}")
    public ResponseEntity<Void> delete(@PathVariable String collection) {
        collectionsService.delete(collection);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Rename collection")
    @PatchMapping("/{collection}")
    public ResponseEntity<Void> rename(@PathVariable String collection,
            @Valid @RequestBody RenameCollectionRequest request) {
        collectionsService.rename(collection, request.newName());
        return ResponseEntity.noContent().build();
    }
}
