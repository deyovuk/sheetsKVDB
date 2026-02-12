package com.example.sheetkv.model;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public record BatchGetRequest(@NotEmpty List<String> ids) {
}
