package com.example.cachingproxy.cache;

public class CacheKeyGenerator {

    public static String generate(String method, String path) {
        return method + ":" + path;
    }
}