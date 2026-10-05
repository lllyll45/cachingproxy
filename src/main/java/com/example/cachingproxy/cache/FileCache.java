package com.example.cachingproxy.cache;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.stream.Stream;

public class FileCache implements Cache {

    private final Path cacheDir;
    private final ObjectMapper objectMapper;

    public FileCache(Path cacheDir) throws IOException {
        this.cacheDir = cacheDir;
        this.objectMapper = new ObjectMapper();
        Files.createDirectories(cacheDir);
    }

    @Override
    public Optional<CachedResponse> get(String key) {
        Path file = cacheDir.resolve(fileNameForKey(key));
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        try {
            CachedResponse response = objectMapper.readValue(file.toFile(), CachedResponse.class);
            return Optional.of(response);
        } catch (IOException e) {
            System.err.println("FileCache: failed to read " + file + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void put(String key, CachedResponse response) {
        Path file = cacheDir.resolve(fileNameForKey(key));
        try {
            objectMapper.writeValue(file.toFile(), response);
        } catch (IOException e) {
            System.err.println("FileCache: failed to write " + file + ": " + e.getMessage());
        }
    }

    @Override
    public void clear() {
        try (Stream<Path> files = Files.list(cacheDir)) {
            files.forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException e) {
                    System.err.println("FileCache: failed to delete " + path + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            System.err.println("FileCache: failed to list " + cacheDir + ": " + e.getMessage());
        }
    }

    private String fileNameForKey(String key) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(key.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex + ".json";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}