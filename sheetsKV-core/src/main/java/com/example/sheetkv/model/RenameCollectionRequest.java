package com.example.sheetkv.model;

import jakarta.validation.constraints.NotBlank;

public record RenameCollectionRequest(@NotBlank String newName) {
}
