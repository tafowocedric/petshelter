package com.petshelter.web.http;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class Response {
    private final HttpExchange exchange;
    private int status = 200;
    private String contentType = "text/html; charset=utf-8";
    private byte[] body = new byte[0];

    public Response(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public Response status(int code) { this.status = code; return this; }
    public Response contentType(String ct) { this.contentType = ct; return this; }
    public Response body(String s) { this.body = s.getBytes(StandardCharsets.UTF_8); return this; }
    public Response body(byte[] b) { this.body = b; return this; }

    public Response html(String html) {
        return contentType("text/html; charset=utf-8").body(html);
    }

    public Response redirect(String location) {
        this.status = 302;
        exchange.getResponseHeaders().add("Location", location);
        this.body = new byte[0];
        return this;
    }

    public Response setCookie(String name, String value) {
        exchange.getResponseHeaders().add("Set-Cookie",
                name + "=" + value + "; Path=/; HttpOnly; SameSite=Strict");
        return this;
    }

    public Response clearCookie(String name) {
        exchange.getResponseHeaders().add("Set-Cookie",
                name + "=; Path=/; Max-Age=0; HttpOnly");
        return this;
    }

    public void send() throws IOException {
        Headers headers = exchange.getResponseHeaders();
        if (!headers.containsKey("Content-Type")) {
            headers.add("Content-Type", contentType);
        }
        exchange.sendResponseHeaders(status, body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }
}