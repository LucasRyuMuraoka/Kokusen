package org.acme.representation;

import org.acme.entity.DomainExpansion;

import java.util.ArrayList;
import java.util.List;

public class DomainExpansionRepresentation {
    public Long id;
    public String name;
    public String effect;
    public Long ownerId;
    public List<LinkRepresentation> links = new ArrayList<>();

    public static DomainExpansionRepresentation from(DomainExpansion d) {
        DomainExpansionRepresentation r = new DomainExpansionRepresentation();
        r.id = d.id;
        r.name = d.name;
        r.effect = d.effect != null ? d.effect : "";
        r.ownerId = d.owner != null ? d.owner.id : null;

        String base = "/domain-expansions/" + d.id;
        r.links = new ArrayList<>();

        // CRUD
        r.links.add(LinkRepresentation.self(base));
        r.links.add(new LinkRepresentation("update", base, "PUT"));
        r.links.add(new LinkRepresentation("delete", base, "DELETE"));

        // Relacionados
        if (d.owner != null) {
            r.links.add(new LinkRepresentation("owner", "/characters/" + d.owner.id, "GET"));
        }

        // Navegação / coleções
        r.links.add(new LinkRepresentation("all-expansions", "/domain-expansions", "GET"));
        r.links.add(new LinkRepresentation("search", "/domain-expansions/search?name=" + d.name, "GET"));

        return r;
    }
}