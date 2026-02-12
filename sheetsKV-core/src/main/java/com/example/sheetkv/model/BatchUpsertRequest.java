package com.example.sheetkv.model;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public record BatchUpsertRequest(@NotEmpty @Valid List<KeyValueEntry> items) {
}
