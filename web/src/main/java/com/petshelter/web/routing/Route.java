package com.petshelter.web.routing;

import com.petshelter.web.controller.Controller;
import com.petshelter.web.http.HttpMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Route {
    private final HttpMethod method;
    private final Pattern pattern;
    private final List<String> paramNames;
    private final Controller controller;

    public Route(HttpMethod method, String pathPattern, Controller controller) {
        this.method = method;
        this.controller = controller;
        this.paramNames = new ArrayList<>();
        this.pattern = compile(pathPattern, paramNames);
    }

    public boolean matches(HttpMethod m, String path) {
        return method == m && pattern.matcher(path).matches();
    }

    public Matcher matcher(String path) {
        return pattern.matcher(path);
    }

    public List<String> paramNames() { return paramNames; }
    public Controller controller() { return controller; }

    private static Pattern compile(String template, List<String> paramNames) {
        StringBuilder sb = new StringBuilder("^");
        for (String segment : template.split("/")) {
            if (segment.isEmpty()) continue;
            sb.append("/");
            if (segment.startsWith(":")) {
                paramNames.add(segment.substring(1));
                sb.append("([^/]+)");
            } else {
                sb.append(Pattern.quote(segment));
            }
        }
        if (sb.length() == 1) sb.append("/");
        sb.append("/?$");
        return Pattern.compile(sb.toString());
    }
}