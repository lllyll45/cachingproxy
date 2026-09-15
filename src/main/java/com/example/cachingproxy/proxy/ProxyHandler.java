package com.example.cachingproxy.proxy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProxyHandler implements HttpHandler {

    private static final Set<String> FORBIDDEN_RESPONSE_HEADERS = Set.of(
            "content-length",
            "transfer-encoding",
            "connection",
            "keep-alive"
    );

    private final OriginClient originClient;

    public ProxyHandler(OriginClient originClient) {
        this.originClient = originClient;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().toString();
            Map<String, List<String>> headers = exchange.getRequestHeaders();
            byte[] body = exchange.getRequestBody().readAllBytes();

            HttpResponse<byte[]> response = originClient.forward(method, path, headers, body);

            exchange.getResponseHeaders().putAll(filterHeaders(response.headers().map()));
            byte[] responseBody = response.body();
            exchange.sendResponseHeaders(response.statusCode(), responseBody.length);
            exchange.getResponseBody().write(responseBody);
            exchange.close();

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            sendError(exchange, 502, "Interrupted");
        } catch (IOException e) {
            sendError(exchange, 502, "Origin unavailable: " + e.getMessage());
        }
    }

    private Map<String, List<String>> filterHeaders(Map<String, List<String>> headers) {
        Map<String, List<String>> filtered = new java.util.HashMap<>();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            if (!FORBIDDEN_RESPONSE_HEADERS.contains(entry.getKey().toLowerCase())) {
                filtered.put(entry.getKey(), entry.getValue());
            }
        }
        return filtered;
    }

    private void sendError(HttpExchange exchange, int status, String message) throws IOException {
        byte[] body = message.getBytes();
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }
}