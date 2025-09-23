package org.acme.representation;

import org.acme.entity.Technique;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TechniqueRepresentation {
    public Long id;
    public String name;
    public String description;
    public List<Long> userIds = new ArrayList<>();
    public List<LinkRepresentation> links = new ArrayList<>();

    public static TechniqueRepresentation from(Technique t) {
        TechniqueRepresentation r = new TechniqueRepresentation();
        r.id = t.id;
        r.name = t.name;
        r.description = t.description != null ? t.description : "";
        r.userIds = t.users != null
                ? t.users.stream().map(u -> u.id).collect(Collectors.toList())
                : new ArrayList<>();

        String base = "/techniques/" + t.id;
        r.links = new ArrayList<>();

        // CRUD
        r.links.add(LinkRepresentation.self(base));
        r.links.add(new LinkRepresentation("update", base, "PUT"));
        r.links.add(new LinkRepresentation("delete", base, "DELETE"));

        // Relacionados
        r.links.add(new LinkRepresentation("users", base + "/users", "GET"));

        // Navegação / coleções
        r.links.add(new LinkRepresentation("all-techniques", "/techniques", "GET"));
        r.links.add(new LinkRepresentation("search", "/techniques/search?name=" + t.name, "GET"));

        return r;
    }
}
