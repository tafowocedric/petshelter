package com.petshelter.web.controller;

import com.petshelter.web.http.Request;
import com.petshelter.web.http.Response;

@FunctionalInterface
public interface Controller {
    Response handle(Request request) throws Exception;
}