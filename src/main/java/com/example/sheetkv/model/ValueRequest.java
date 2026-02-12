package com.example.sheetkv.model;

import jakarta.validation.constraints.NotNull;

public record ValueRequest(@NotNull String value) {
}
