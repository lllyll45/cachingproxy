package com.example.cachingproxy;

import com.example.cachingproxy.cache.Cache;
import com.example.cachingproxy.cache.FileCache;
import com.example.cachingproxy.cli.CliArgs;
import com.example.cachingproxy.cli.CliParser;
import com.example.cachingproxy.proxy.OriginClient;
import com.example.cachingproxy.proxy.ProxyHandler;
import com.example.cachingproxy.server.ProxyServer;

import java.io.IOException;
import java.nio.file.Path;

public class Main {

    private static final Path CACHE_DIR = Path.of("./cache");

    public static void main(String[] args) {
        try {
            CliArgs parsed = CliParser.parse(args);
            if (parsed.clearCache()) {
                Cache cache = new FileCache(CACHE_DIR);
                cache.clear();
                System.out.println("Cache cleared: " + CACHE_DIR.toAbsolutePath());
            } else {
                OriginClient originClient = new OriginClient(parsed.origin());
                Cache cache = new FileCache(CACHE_DIR);
                ProxyHandler handler = new ProxyHandler(originClient, cache);
                ProxyServer server = new ProxyServer(parsed.port(), handler);
                server.start();
                System.out.println("Proxy started on port " + parsed.port()
                        + ", origin " + parsed.origin());
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            System.err.println("Usage:");
            System.err.println("  caching-proxy --port <number> --origin <url>");
            System.err.println("  caching-proxy --clear-cache");
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
            System.exit(1);
        }
    }
}