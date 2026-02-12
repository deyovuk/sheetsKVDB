package com.example.sheetkv.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sheetkv.exception.BadRequestException;
import com.example.sheetkv.model.BatchDeleteRequest;
import com.example.sheetkv.model.BatchDeleteResult;
import com.example.sheetkv.model.BatchGetRequest;
import com.example.sheetkv.model.BatchGetResult;
import com.example.sheetkv.model.BatchUpsertRequest;
import com.example.sheetkv.model.BatchUpsertResult;
import com.example.sheetkv.model.KeyValueEntry;
import com.example.sheetkv.model.PageResult;
import com.example.sheetkv.model.ValueRequest;
import com.example.sheetkv.service.KvService;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/v1/collections/{collection}")
@Validated
public class KvController {
    private final KvService kvService;

    public KvController(KvService kvService) {
        this.kvService = kvService;
    }

    @Operation(summary = "Get value")
    @GetMapping("/keys/{id}")
    public ResponseEntity<KeyValueEntry> get(@PathVariable String collection, @PathVariable String id) {
        String value = kvService.get(collection, id);
        return ResponseEntity.ok(new KeyValueEntry(id, value));
    }

    @Operation(summary = "Upsert value")
    @PutMapping("/keys/{id}")
    public ResponseEntity<Void> upsert(@PathVariable String collection,
            @PathVariable String id,
            @Valid @RequestBody ValueRequest request) {
        kvService.upsert(collection, id, request.value());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Delete value")
    @DeleteMapping("/keys/{id}")
    public ResponseEntity<Void> delete(@PathVariable String collection, @PathVariable String id) {
        kvService.delete(collection, id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "List keys")
    @GetMapping("/keys")
    public ResponseEntity<PageResult<String>> listKeys(@PathVariable String collection,
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit,
            @RequestParam(required = false) String cursor) {
        int offset = parseCursor(cursor);
        return ResponseEntity.ok(kvService.listKeys(collection, limit, offset));
    }

    @Operation(summary = "List entries")
    @GetMapping("/entries")
    public ResponseEntity<PageResult<KeyValueEntry>> listEntries(@PathVariable String collection,
            @RequestParam(defaultValue = "100") @Min(1) @Max(1000) int limit,
            @RequestParam(required = false) String cursor) {
        int offset = parseCursor(cursor);
        return ResponseEntity.ok(kvService.listEntries(collection, limit, offset));
    }

    @Operation(summary = "Batch get")
    @PostMapping("/batchGet")
    public ResponseEntity<BatchGetResult> batchGet(@PathVariable String collection,
            @Valid @RequestBody BatchGetRequest request) {
        return ResponseEntity.ok(kvService.batchGet(collection, request.ids()));
    }

    @Operation(summary = "Batch upsert")
    @PostMapping("/batchUpsert")
    public ResponseEntity<BatchUpsertResult> batchUpsert(@PathVariable String collection,
            @Valid @RequestBody BatchUpsertRequest request) {
        return ResponseEntity.ok(kvService.batchUpsert(collection, request.items()));
    }

    @Operation(summary = "Batch delete")
    @PostMapping("/batchDelete")
    public ResponseEntity<BatchDeleteResult> batchDelete(@PathVariable String collection,
            @Valid @RequestBody BatchDeleteRequest request) {
        return ResponseEntity.ok(kvService.batchDelete(collection, request.ids()));
    }

    private int parseCursor(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return 0;
        }
        try {
            int value = Integer.parseInt(cursor);
            if (value < 0) {
                throw new BadRequestException("cursor must be >= 0");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new BadRequestException("Invalid cursor");
        }
    }
}
