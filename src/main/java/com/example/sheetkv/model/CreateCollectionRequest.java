package com.example.sheetkv.model;

import jakarta.validation.constraints.NotBlank;

public record CreateCollectionRequest(@NotBlank String name) {
}
