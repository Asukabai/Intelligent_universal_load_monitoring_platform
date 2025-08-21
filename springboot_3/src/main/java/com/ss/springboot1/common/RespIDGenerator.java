package com.ss.springboot1.common;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class RespIDGenerator{
    private static final ConcurrentHashMap<String, AtomicInteger> COUNTERS = new ConcurrentHashMap<>();

    public static int next(String key) {
        return COUNTERS.computeIfAbsent(key, k -> new AtomicInteger(1)).getAndIncrement();
    }
}
