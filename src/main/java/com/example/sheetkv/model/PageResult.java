package com.example.sheetkv.model;

import java.util.List;

public record PageResult<T>(List<T> items, String nextCursor) {
}
