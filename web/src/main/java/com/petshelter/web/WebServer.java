package com.petshelter.web;

import com.petshelter.repository.UserRepository;
import com.petshelter.service.AuthService;
import com.petshelter.web.controller.AuthController;
import com.petshelter.web.http.Response;
import com.petshelter.web.middleware.Guards;
import com.petshelter.web.routing.Router;
import com.petshelter.web.session.CurrentUser;
import com.petshelter.web.session.SessionManager;
import com.petshelter.web.view.AdminDashboardView;
import com.petshelter.web.view.BrowseView;
import com.petshelter.web.view.HomeView;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServer {

    private final int port;
    private final Router router = new Router();
    private final SessionManager sessions = new SessionManager();
    private final AuthController authController;
    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
        AuthService authService = new AuthService(new UserRepository());
        this.authController = new AuthController(authService, sessions);
        registerRoutes();
    }

    private void registerRoutes() {
        router.get("/", req -> new Response(req.exchange()).html(
                HomeView.render(CurrentUser.from(req, sessions).orElse(null))
        ));

        router.get("/login",     authController::showLogin);
        router.post("/login",    authController::doLogin);
        router.get("/register",  authController::showRegister);
        router.post("/register", authController::doRegister);
        router.post("/logout",   authController::logout);

        router.get("/admin", Guards.adminOnly(sessions, req ->
                new Response(req.exchange()).html(
                        AdminDashboardView.render(CurrentUser.from(req, sessions).orElseThrow())
                )
        ));

        router.get("/browse", Guards.authenticated(sessions, req ->
                new Response(req.exchange()).html(
                        BrowseView.render(CurrentUser.from(req, sessions).orElseThrow())
                )
        ));
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