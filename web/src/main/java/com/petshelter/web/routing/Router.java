package com.petshelter.web.routing;

import com.petshelter.web.controller.Controller;
import com.petshelter.web.http.HttpMethod;
import com.petshelter.web.http.Request;
import com.petshelter.web.http.Response;
import com.petshelter.web.session.CurrentUser;
import com.petshelter.web.session.SessionManager;
import com.petshelter.web.view.ErrorView;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class Router implements HttpHandler {

    private final List<Route> routes = new ArrayList<>();
    private final SessionManager sessions;

    public Router(SessionManager sessions) {
        this.sessions = sessions;
    }

    public Router get(String path, Controller c)    { return add(HttpMethod.GET, path, c); }
    public Router post(String path, Controller c)   { return add(HttpMethod.POST, path, c); }
    public Router put(String path, Controller c)    { return add(HttpMethod.PUT, path, c); }
    public Router delete(String path, Controller c) { return add(HttpMethod.DELETE, path, c); }

    private Router add(HttpMethod method, String path, Controller c) {
        routes.add(new Route(method, path, c));
        return this;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Request request = Request.from(exchange);
        Response response = new Response(exchange);

        try {
            for (Route route : routes) {
                if (route.matches(request.method(), request.path())) {
                    Matcher m = route.matcher(request.path());
                    if (m.matches()) {
                        for (int i = 0; i < route.paramNames().size(); i++) {
                            request.setPathParam(route.paramNames().get(i), m.group(i + 1));
                        }
                    }
                    Response result = route.controller().handle(request);
                    if (result != null) result.send();
                    else response.status(204).send();
                    return;
                }
            }
            response.status(404).html(ErrorView.notFound(CurrentUser.from(request, sessions).orElse(null),
                request.path())).send();
        } catch (Exception e) {
            e.printStackTrace();
            response.status(500).html(ErrorView.serverError(CurrentUser.from(request, sessions).orElse(null),
                e.getMessage())).send();
        }
    }
}