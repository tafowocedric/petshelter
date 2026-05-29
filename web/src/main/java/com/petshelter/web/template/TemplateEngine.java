package com.petshelter.web.template;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.TemplateLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TemplateEngine {
    private final Handlebars handlebars;

    public TemplateEngine() {
        TemplateLoader loader = new ClassPathTemplateLoader("/templates", ".hbs");
        this.handlebars = new Handlebars(loader);
    }

    public String render(String templateName, Map<String, Object> context) {
        try {
            Template t = handlebars.compile(templateName);
            return t.apply(context);
        } catch (IOException e) {
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }

    public String render(String templateName) {
        return render(templateName, new HashMap<>());
    }
}