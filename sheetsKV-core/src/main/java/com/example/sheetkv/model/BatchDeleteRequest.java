package com.example.sheetkv.model;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record BatchDeleteRequest(@NotEmpty List<String> ids) {
}
