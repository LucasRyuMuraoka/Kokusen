package org.acme.dto;

import org.acme.representation.CharacterRepresentation;

import java.util.List;

public class SearchCharacterResponse {
    public List<CharacterRepresentation> characters;
    public long totalCharacters;
    public int totalPages;
    public boolean hasMore;
    public String nextPage;

    public SearchCharacterResponse() {}
}
