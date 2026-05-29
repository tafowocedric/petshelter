package com.petshelter.web;

import com.petshelter.web.routing.Router;
import com.petshelter.web.template.TemplateEngine;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class WebServer {
    private final int port;
    private final Router router;
    private final TemplateEngine templates;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
        this.router = new Router();
        this.templates = new TemplateEngine();
        registerDefaultRoutes();
    }

    public Router router() { return router; }
    public TemplateEngine templates() { return templates; }

    private void registerDefaultRoutes() {
        router.get("/", req -> {
            Map<String, Object> ctx = new HashMap<>();
            ctx.put("title", "Pet Shelter");
            ctx.put("message", "Welcome to the Pet Shelter Management System!");
            return new com.petshelter.web.http.Response(req.exchange())
                    .html(templates.render("home", ctx));
        });
    }

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/public", new StaticHandler("/public"));
        server.createContext("/", router);
        server.setExecutor(null);
        server.start();
        System.out.println("[Web] Server listening on http://localhost:" + port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            System.out.println("[Web] Server stopped");
        }
    }
}