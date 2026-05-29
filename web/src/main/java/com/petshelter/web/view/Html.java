package com.petshelter.web.view;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class Html {
    public interface Node {
        void render(StringBuilder sb);

        default String toHtml() {
            StringBuilder sb = new StringBuilder();
            render(sb);
            return sb.toString();
        }
    }

    public static Element tag(String name)              { return new Element(name); }
    public static Element div()                          { return tag("div"); }
    public static Element span()                         { return tag("span"); }
    public static Element section()                      { return tag("section"); }
    public static Element header()                       { return tag("header"); }
    public static Element nav()                          { return tag("nav"); }
    public static Element main_()                        { return tag("main"); }
    public static Element h1(String t)                   { return tag("h1").text(t); }
    public static Element h2(String t)                   { return tag("h2").text(t); }
    public static Element h3(String t)                   { return tag("h3").text(t); }
    public static Element p(String t)                    { return tag("p").text(t); }
    public static Element strong(String t)               { return tag("strong").text(t); }
    public static Element a(String href, String text)    { return tag("a").attr("href", href).text(text); }
    public static Element button(String text)            { return tag("button").type("submit").text(text); }
    public static Element form()                         { return tag("form"); }
    public static Element label(String text)             { return tag("label").text(text); }
    public static Element input()                        { return tag("input").selfClosing(); }
    public static Element table()                        { return tag("table"); }
    public static Element thead()                        { return tag("thead"); }
    public static Element tbody()                        { return tag("tbody"); }
    public static Element tr()                           { return tag("tr"); }
    public static Element th(String text)                { return tag("th").text(text); }
    public static Element td(String text)                { return tag("td").text(text); }
    public static Element ul()                           { return tag("ul"); }
    public static Element li(String text)                { return tag("li").text(text); }
    public static Element select(String name)            { return tag("select").attr("name", name); }
    public static Element option(String value, String text) { return tag("option").attr("value", value).text(text); }
    public static Element textarea(String name)          { return tag("textarea").attr("name", name); }

    public static Node text(String s)                    { return new TextNode(s, true); }
    public static Node raw(String s)                     { return new TextNode(s, false); }
    public static Node empty()                           { return sb -> {}; }

    public static Node each(Iterable<? extends Node> items) {
        return sb -> { for (Node n : items) n.render(sb); };
    }

    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&':  out.append("&amp;");  break;
                case '<':  out.append("&lt;");   break;
                case '>':  out.append("&gt;");   break;
                case '"':  out.append("&quot;"); break;
                case '\'': out.append("&#39;");  break;
                default:   out.append(c);
            }
        }
        return out.toString();
    }

    public static final class Element implements Node {
        private final String name;
        private final Map<String, String> attrs = new LinkedHashMap<>();
        private final List<Node> children = new ArrayList<>();
        private boolean selfClosing = false;
        private String textContent;

        Element(String name) { this.name = name; }

        public Element attr(String k, String v) { attrs.put(k, v); return this; }
        public Element cls(String c)            { return attr("class", c); }
        public Element id(String v)             { return attr("id", v); }
        public Element name(String v)           { return attr("name", v); }
        public Element type(String v)           { return attr("type", v); }
        public Element value(String v)          { return attr("value", v); }
        public Element href(String v)           { return attr("href", v); }
        public Element method(String v)         { return attr("method", v); }
        public Element action(String v)         { return attr("action", v); }
        public Element placeholder(String v)    { return attr("placeholder", v); }
        public Element required()               { return attr("required", "required"); }
        public Element autofocus()              { return attr("autofocus", "autofocus"); }
        public Element selfClosing()            { this.selfClosing = true; return this; }

        public Element text(String t)           { this.textContent = t; return this; }

        public Element with(Node... nodes) {
            for (Node n : nodes) if (n != null) children.add(n);
            return this;
        }

        @Override
        public void render(StringBuilder sb) {
            sb.append("<").append(name);
            for (Map.Entry<String, String> e : attrs.entrySet()) {
                sb.append(' ').append(e.getKey()).append("=\"").append(escape(e.getValue())).append('"');
            }
            if (selfClosing && children.isEmpty() && textContent == null) {
                sb.append(">");
                return;
            }
            sb.append(">");
            if (textContent != null) sb.append(escape(textContent));
            for (Node child : children) child.render(sb);
            sb.append("</").append(name).append(">");
        }
    }

    private static final class TextNode implements Node {
        private final String content;
        private final boolean escape;

        TextNode(String content, boolean escape) {
            this.content = content == null ? "" : content;
            this.escape = escape;
        }

        @Override
        public void render(StringBuilder sb) {
            sb.append(escape ? escape(content) : content);
        }
    }

    private Html() {}
}