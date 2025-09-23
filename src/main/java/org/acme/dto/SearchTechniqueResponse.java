package org.acme.dto;

import org.acme.representation.TechniqueRepresentation;

import java.util.List;

public class SearchTechniqueResponse {
    public List<TechniqueRepresentation> techniques;
    public long totalTechniques;
    public int totalPages;
    public boolean hasMore;
    public String nextPage;

    public SearchTechniqueResponse() {}
}
