package com.example.cachingproxy.proxy;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OriginClient {

    private static final Set<String> FORBIDDEN_HEADERS = Set.of(
            "host",
            "content-length",
            "connection",
            "transfer-encoding",
            "upgrade",
            "keep-alive"
    );

    private final HttpClient httpClient;
    private final String baseUrl;

    public OriginClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public HttpResponse<byte[]> forward(String method,
                                        String path,
                                        Map<String, List<String>> headers,
                                        byte[] body) throws IOException, InterruptedException {

        URI uri = URI.create(baseUrl + path);
        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(uri);

        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String name = entry.getKey();
            if (FORBIDDEN_HEADERS.contains(name.toLowerCase())) {
                continue;
            }
            for (String value : entry.getValue()) {
                builder.header(name, value);
            }
        }

        HttpRequest.BodyPublisher bodyPublisher;
        if (body == null || body.length == 0) {
            bodyPublisher = HttpRequest.BodyPublishers.noBody();
        } else {
            bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(body);
        }

        builder.method(method, bodyPublisher);
        HttpRequest request = builder.build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
    }
}