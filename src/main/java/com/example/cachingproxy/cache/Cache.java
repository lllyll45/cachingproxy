package com.example.cachingproxy.cache;


import java.util.Optional;

public interface Cache {

    Optional<CachedResponse> get(String key);

    void put(String key, CachedResponse response);

    void clear();

}
