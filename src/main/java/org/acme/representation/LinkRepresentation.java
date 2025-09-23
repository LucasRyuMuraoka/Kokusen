package org.acme.representation;

public class LinkRepresentation {
    public String rel;
    public String href;
    public String method;

    public LinkRepresentation() {}

    public LinkRepresentation(String rel, String href) {
        this.rel = rel;
        this.href = href;
    }

    public LinkRepresentation(String rel, String href, String method) {
        this.rel = rel;
        this.href = href;
        this.method = method;
    }

    public static LinkRepresentation self(String href) {
        return new LinkRepresentation("self", href, "GET");
    }

    public static LinkRepresentation of(String rel, String href, String method) {
        return new LinkRepresentation(rel, href, method);
    }
}