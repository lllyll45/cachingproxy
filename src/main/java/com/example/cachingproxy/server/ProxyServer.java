package com.example.cachingproxy.server;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ProxyServer {

    private final HttpHandler handler;
    private final HttpServer httpServer;

    public ProxyServer(int port, HttpHandler handler) throws IOException {
        InetSocketAddress address = new InetSocketAddress(port);
        this.httpServer = HttpServer.create(address, 0);
        this.handler = handler;
    }


    public void start(){
        httpServer.createContext("/", handler);
        httpServer.start();
    }

    public void stop(){
        httpServer.stop(0);
    }
}
