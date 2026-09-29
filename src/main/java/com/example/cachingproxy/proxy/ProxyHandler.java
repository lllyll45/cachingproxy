package com.example.cachingproxy.proxy;

import com.example.cachingproxy.cache.Cache;
import com.example.cachingproxy.cache.CacheKeyGenerator;
import com.example.cachingproxy.cache.CachedResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ProxyHandler implements HttpHandler {

    private static final Set<String> FORBIDDEN_RESPONSE_HEADERS = Set.of(
            "content-length",
            "transfer-encoding",
            "connection",
            "keep-alive"
    );

    private final OriginClient originClient;
    private final Cache cache;

    public ProxyHandler(OriginClient originClient, Cache cache) {
        this.originClient = originClient;
        this.cache = cache;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().toString();
            Map<String, List<String>> headers = exchange.getRequestHeaders();
            byte[] body = exchange.getRequestBody().readAllBytes();

            String key = CacheKeyGenerator.generate(method, path);

            Optional<CachedResponse> cached = cache.get(key);

            if (cached.isPresent()) {
                CachedResponse response = cached.get();
                writeResponse(exchange, response.status(),
                        response.headers(), response.body(), "HIT");
                return;
            }

            HttpResponse<byte[]> originResponse = originClient.forward(method, path, headers, body);

            Map<String, List<String>> filteredHeaders = filterHeaders(originResponse.headers().map());
            byte[] responseBody = originResponse.body();
            int status = originResponse.statusCode();

            if (isCacheable(method, status)) {
                CachedResponse toCache = new CachedResponse(status, filteredHeaders, responseBody);
                cache.put(key, toCache);
            }

            writeResponse(exchange, status, filteredHeaders, responseBody, "MISS");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendError(exchange, 502, "Interrupted");
        } catch (IOException e) {
            sendError(exchange, 502, "Origin unavailable: " + e.getMessage());
        }
    }

    private boolean isCacheable(String method, int status) {
        return "GET".equalsIgnoreCase(method) && status >= 200 && status < 300;
    }

    private Map<String, List<String>> filterHeaders(Map<String, List<String>> headers) {
        Map<String, List<String>> filtered = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (!FORBIDDEN_RESPONSE_HEADERS.contains(entry.getKey().toLowerCase())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private void writeResponse(HttpExchange exchange,
                               int status,
                               Map<String, List<String>> headers,
                               byte[] body,
                               String cacheStatus) throws IOException {
        exchange.getResponseHeaders().putAll(headers);
        exchange.getResponseHeaders().set("X-Cache", cacheStatus);
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        byte[] body = message.getBytes();
        exchange.getResponseHeaders().set("X-Cache", "MISS");
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}