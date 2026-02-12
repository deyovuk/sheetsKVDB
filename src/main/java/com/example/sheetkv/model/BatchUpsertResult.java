package com.example.sheetkv.model;

import java.util.List;

public record BatchUpsertResult(List<String> created, List<String> updated) {
}
