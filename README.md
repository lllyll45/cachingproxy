
# Caching Proxy

A CLI tool that starts a caching proxy server, forwards requests to an origin, and caches responses. Built with pure JDK — no frameworks.
URL: https://roadmap.sh/projects/caching-server
## Features
- Forwards HTTP requests to a configurable origin server
- Caches `GET` responses (2xx only) with `X-Cache: HIT` / `X-Cache: MISS` headers
- Persistent file-based cache — survives restarts
- `--clear-cache` flag to wipe the cache
- Pure JDK: `com.sun.net.httpserver.HttpServer` + `java.net.http.HttpClient`
- No Spring, no Netty, no external HTTP libraries

## Requirements

- Java 25+
- Maven 3.9+

## Build

```bash
mvn clean package
```

Produces an executable jar: `target/cachingproxy-1.0-SNAPSHOT.jar` (includes all dependencies).

## Usage

### Start the proxy

```bash
caching-proxy --port 3000 --origin https://dummyjson.com
```

Output:

```
Proxy started on port 3000, origin https://dummyjson.com
```

### Make requests

```bash
curl -i http://localhost:3000/products
```

First request → `X-Cache: MISS` (fetched from origin).

```bash
curl -i http://localhost:3000/products
```

Second request → `X-Cache: HIT` (served from cache, origin not contacted).

### Clear the cache

```bash
caching-proxy --clear-cache
```

Output:

```
Cache cleared: /path/to/project/cache
```

## Options

`--port <number>` Port to listen on (1..65535) 
`--origin <url>` Origin server to forward requests to 
`--clear-cache` Clear the cache and exit 

## Demo

```bash
$ caching-proxy --port 3000 --origin https://dummyjson.com
Proxy started on port 3000, origin https://dummyjson.com

$ curl -i http://localhost:3000/products
HTTP/1.1 200 OK
X-cache: MISS
Content-type: application/json; charset=utf-8
...

$ curl -i http://localhost:3000/products
HTTP/1.1 200 OK
X-cache: HIT
Content-type: application/json; charset=utf-8
...
```

