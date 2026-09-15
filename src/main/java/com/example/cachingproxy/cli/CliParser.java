package com.example.cachingproxy.cli;

public class CliParser {

    public static CliArgs parse(String[] args) {
        int port = -1;
        String origin = null;
        boolean clearCache = false;

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            switch (arg) {
                case "--port" -> {
                    if (port != -1) {
                        throw new IllegalArgumentException("--port specified twice");
                    }
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--port requires a value");
                    }
                    String value = args[i + 1];
                    try {
                        port = Integer.parseInt(value);
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("--port must be a number, got: " + value);
                    }
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException("--port must be in range 1..65535, got: " + port);
                    }
                    i++;
                }
                case "--origin" -> {
                    if (origin != null) {
                        throw new IllegalArgumentException("--origin specified twice");
                    }
                    if (i + 1 >= args.length) {
                        throw new IllegalArgumentException("--origin requires a value");
                    }
                    origin = args[i + 1];
                    if (origin.isEmpty()) {
                        throw new IllegalArgumentException("--origin must not be empty");
                    }
                    i++;
                }
                case "--clear-cache" -> {
                    if (clearCache) {
                        throw new IllegalArgumentException("--clear-cache specified twice");
                    }
                    clearCache = true;
                }
                default -> throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }

        if (clearCache) {
            return new CliArgs(0, null, true);
        }
        if (port == -1) {
            throw new IllegalArgumentException("--port is required (or use --clear-cache)");
        }
        if (origin == null) {
            throw new IllegalArgumentException("--origin is required (or use --clear-cache)");
        }
        return new CliArgs(port, origin, false);
    }
}