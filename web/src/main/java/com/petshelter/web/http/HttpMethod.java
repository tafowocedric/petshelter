package com.petshelter.web.http;

public enum HttpMethod {
    GET, POST, PUT, DELETE, PATCH;

    public static HttpMethod from(String s) {
        try {
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GET;
        }
    }
}