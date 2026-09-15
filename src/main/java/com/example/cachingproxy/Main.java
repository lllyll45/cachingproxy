package com.example.cachingproxy;

import com.example.cachingproxy.cli.CliArgs;
import com.example.cachingproxy.cli.CliParser;
import com.example.cachingproxy.proxy.OriginClient;
import com.example.cachingproxy.proxy.ProxyHandler;
import com.example.cachingproxy.server.ProxyServer;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            CliArgs parsed = CliParser.parse(args);
            if (parsed.clearCache()) {
                System.out.println("Clear cache mode");
                // TODO: очистить кэш (шаг 4)
            } else {
                OriginClient originClient = new OriginClient(parsed.origin());
                ProxyHandler handler = new ProxyHandler(originClient);
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