package org.acme.dto;

import org.acme.representation.DomainExpansionRepresentation;

import java.util.List;

public class SearchDomainExpansionResponse {
    public List <DomainExpansionRepresentation> domainExpansions;
    public long totalDomainExpansions;
    public int totalPages;
    public boolean hasMore;
    public String nextPage;

    public SearchDomainExpansionResponse() {}
}