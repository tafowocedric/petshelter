package com.petshelter.web.http;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Request {
    private final HttpExchange exchange;
    private final HttpMethod method;
    private final String path;
    private final Map<String, String> query;
    private final Map<String, String> form;
    private final Map<String, String> pathParams;
    private final Map<String, String> cookies;

    private Request(HttpExchange exchange, HttpMethod method, String path, Map<String, String> query, Map<String, String> form, Map<String, String> cookies) {
        this.exchange = exchange;
        this.method = method;
        this.path = path;
        this.query = query;
        this.form = form;
        this.cookies = cookies;
        this.pathParams = new HashMap<>();
    }

    public static Request from(HttpExchange exchange) throws IOException {
        HttpMethod method = HttpMethod.from(exchange.getRequestMethod());
        String fullUri = exchange.getRequestURI().toString();
        String path = fullUri.split("\\?", 2)[0];
        String rawQuery = exchange.getRequestURI().getRawQuery();

        Map<String, String> query = parseUrlEncoded(rawQuery);
        Map<String, String> form = new HashMap<>();
        if (method == HttpMethod.POST || method == HttpMethod.PUT) {
            String body = readBody(exchange.getRequestBody());
            form = parseUrlEncoded(body);
        }
        Map<String, String> cookies = parseCookies(exchange.getRequestHeaders().getFirst("Cookie"));

        return new Request(exchange, method, path, query, form, cookies);
    }

    public HttpMethod method() { return method; }
    public String path() { return path; }
    public String query(String key) { return query.get(key); }
    public String form(String key) { return form.get(key); }
    public String param(String key) { return pathParams.get(key); }
    public String cookie(String key) { return cookies.get(key); }

    public void setPathParam(String key, String value) {
        pathParams.put(key, value);
    }

    public HttpExchange exchange() { return exchange; }

    private static String readBody(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static Map<String, String> parseUrlEncoded(String s) {
        Map<String, String> out = new HashMap<>();
        if (s == null || s.isEmpty()) return out;
        for (String pair : s.split("&")) {
            String[] kv = pair.split("=", 2);
            String k = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
            String v = kv.length > 1 ? URLDecoder.decode(kv[1], StandardCharsets.UTF_8) : "";
            out.put(k, v);
        }
        return out;
    }

    private static Map<String, String> parseCookies(String header) {
        Map<String, String> out = new HashMap<>();
        if (header == null) return out;
        for (String c : header.split(";")) {
            String[] kv = c.trim().split("=", 2);
            if (kv.length == 2) out.put(kv[0], kv[1]);
        }
        return out;
    }
}