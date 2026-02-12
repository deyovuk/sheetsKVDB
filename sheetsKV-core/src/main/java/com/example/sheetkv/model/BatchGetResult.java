package com.example.sheetkv.model;

import java.util.List;

public record BatchGetResult(List<KeyValueEntry> found, List<String> missing) {
}
