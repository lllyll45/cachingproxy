package com.example.cachingproxy.cli;

public record CliArgs(int port, String origin, boolean clearCache) {}
