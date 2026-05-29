package com.petshelter.web;

import com.petshelter.enums.AdoptionStatus;
import com.petshelter.enums.AnimalStatus;
import com.petshelter.repository.AdoptionRepository;
import com.petshelter.repository.AnimalRepository;
import com.petshelter.repository.UserRepository;
import com.petshelter.service.AnimalService;
import com.petshelter.service.AuthService;
import com.petshelter.web.controller.AnimalController;
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

    private final AnimalRepository animalRepo = new AnimalRepository();
    private final UserRepository userRepo = new UserRepository();
    private final AdoptionRepository adoptionRepo = new AdoptionRepository();
    private final AuthService authService = new AuthService(userRepo);
    private final AnimalService animalService = new AnimalService(animalRepo, authService);

    private final AuthController authController = new AuthController(authService, sessions);
    private final AnimalController animalController = new AnimalController(animalService, sessions);

    private HttpServer server;

    public WebServer(int port) {
        this.port = port;
        registerRoutes();
    }

    private void registerRoutes() {
        router.get("/", req -> new Response(req.exchange()).html(
            HomeView.render(CurrentUser.from(req, sessions).orElse(null))
        ));

        // Auth
        router.get("/login", authController::showLogin);
        router.post("/login", authController::doLogin);
        router.get("/register", authController::showRegister);
        router.post("/register", authController::doRegister);
        router.post("/logout", authController::logout);

        // Admin dashboard
        router.get("/admin", Guards.adminOnly(sessions, req -> {
            var current = CurrentUser.from(req, sessions).orElseThrow();
            long total = animalRepo.count();
            long avail = animalRepo.countByStatus(AnimalStatus.AVAILABLE);
            long adopted = animalRepo.countByStatus(AnimalStatus.ADOPTED);
            long pending = adoptionRepo.findByStatus(AdoptionStatus.PENDING).size();
            return new Response(req.exchange()).html(AdminDashboardView.render(current, total, avail, adopted, pending));
        }));

        // Admin — animals CRUD
        router.get("/admin/animals", Guards.adminOnly(sessions, animalController::list));
        router.get("/admin/animals/new", Guards.adminOnly(sessions, animalController::newForm));
        router.post("/admin/animals/new", Guards.adminOnly(sessions, animalController::create));
        router.get("/admin/animals/:id/edit", Guards.adminOnly(sessions, animalController::editForm));
        router.post("/admin/animals/:id/edit", Guards.adminOnly(sessions, animalController::update));
        router.post("/admin/animals/:id/delete", Guards.adminOnly(sessions, animalController::delete));

        // Client
        router.get("/browse", Guards.authenticated(sessions, req ->
            new Response(req.exchange()).html(BrowseView.render(CurrentUser.from(req, sessions).orElseThrow()))
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