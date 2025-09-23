package org.acme.representation;

import org.acme.entity.Character;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CharacterRepresentation {
    public Long id;
    public String name;
    public String rank;
    public String clanName;
    public List<String> techniques = new ArrayList<>();
    public String domainExpansionName;
    public List<LinkRepresentation> links = new ArrayList<>();

    public static CharacterRepresentation from(Character c) {
        CharacterRepresentation r = new CharacterRepresentation();
        r.id = c.id;
        r.name = c.name;
        r.rank = c.rank != null ? c.rank.name() : null;
        r.clanName = c.clan != null ? c.clan.name : null;
        r.techniques = c.techniques != null
                ? c.techniques.stream().map(t -> t.name).collect(Collectors.toList())
                : new ArrayList<>();
        r.domainExpansionName = c.domainExpansion != null ? c.domainExpansion.name : null;

        String base = "/characters/" + c.id;
        r.links = new ArrayList<>();

        // CRUD
        r.links.add(LinkRepresentation.self(base));
        r.links.add(new LinkRepresentation("update", base, "PUT"));
        r.links.add(new LinkRepresentation("delete", base, "DELETE"));

        // Relacionados
        r.links.add(new LinkRepresentation("techniques", base + "/techniques", "GET"));
        r.links.add(new LinkRepresentation("domain-expansion", "/domain-expansions/by-character/" + c.id, "GET"));

        // Navegação / coleções
        r.links.add(new LinkRepresentation("all-characters", "/characters", "GET"));
        r.links.add(new LinkRepresentation("search", "/characters/search?name=" + c.name, "GET"));

        // Extras
        if (c.rank != null) {
            r.links.add(new LinkRepresentation("by-rank", "/characters/rank/" + c.rank.name(), "GET"));
        }
        if (c.clan != null) {
            r.links.add(new LinkRepresentation("by-clan", "/characters/clan/" + c.clan.name, "GET"));
        }

        return r;
    }
}