package com.petshelter.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StaticHandler implements HttpHandler {
    private final String classpathRoot;

    public StaticHandler(String classpathRoot) {
        this.classpathRoot = classpathRoot;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String resource = classpathRoot + path.substring("/public".length());

        try (InputStream in = getClass().getResourceAsStream(resource)) {
            if (in == null) {
                byte[] notFound = "Not Found".getBytes();
                exchange.sendResponseHeaders(404, notFound.length);
                exchange.getResponseBody().write(notFound);
                exchange.close();
                return;
            }
            byte[] data = in.readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", guessType(path));
            exchange.sendResponseHeaders(200, data.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(data);
            }
        }
    }

    private String guessType(String path) {
        if (path.endsWith(".css"))  return "text/css; charset=utf-8";
        if (path.endsWith(".js"))   return "application/javascript; charset=utf-8";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg"))  return "image/svg+xml";
        return "application/octet-stream";
    }
}