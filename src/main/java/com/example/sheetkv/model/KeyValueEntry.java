package com.example.sheetkv.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record KeyValueEntry(
        @NotBlank String id,
        @NotNull String value) {
}
