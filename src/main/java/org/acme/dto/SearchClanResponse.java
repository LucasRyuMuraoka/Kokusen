package org.acme.dto;

import org.acme.representation.ClanRepresentation;

import java.util.ArrayList;
import java.util.List;

public class SearchClanResponse {
    public List<ClanRepresentation> clans = new ArrayList<>();
    public long totalClans;
    public int totalPages;
    public boolean hasMore;
    public String nextPage;

    public SearchClanResponse() {}
}
