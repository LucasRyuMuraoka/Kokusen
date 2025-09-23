package org.acme.representation;

import org.acme.entity.Clan;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ClanRepresentation {
    public Long id;
    public String name;
    public String description;
    public List<Long> memberIds = new ArrayList<>();
    public List<LinkRepresentation> links = new ArrayList<>();

    public static ClanRepresentation from(Clan c) {
        ClanRepresentation r = new ClanRepresentation();
        r.id = c.id;
        r.name = c.name;
        r.description = c.description != null ? c.description : "";
        r.memberIds = c.members != null
                ? c.members.stream().map(m -> m.id).collect(Collectors.toList())
                : new ArrayList<>();

        String base = "/clans/" + c.id;
        r.links = new ArrayList<>();

        // CRUD
        r.links.add(LinkRepresentation.self(base));
        r.links.add(new LinkRepresentation("update", base, "PUT"));
        r.links.add(new LinkRepresentation("delete", base, "DELETE"));

        // Relacionados
        r.links.add(new LinkRepresentation("members", base + "/members", "GET"));

        // Navegação / coleções
        r.links.add(new LinkRepresentation("all-clans", "/clans", "GET"));
        r.links.add(new LinkRepresentation("search", "/clans/search?name=" + c.name, "GET"));

        return r;
    }
}