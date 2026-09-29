package com.example.cachingproxy.cache;


import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryCache implements Cache{

    private final Map<String, CachedResponse> store = new ConcurrentHashMap<>();

    @Override
    public Optional<CachedResponse> get(String key) {
        return Optional.ofNullable(store.get(key));
    }

    @Override
    public void put(String key, CachedResponse response) {
        store.put(key, response);
    }

    @Override
    public void clear() {
        store.clear();
    }

}
