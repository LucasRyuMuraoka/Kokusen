package org.acme.resource;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.validation.Valid;
import org.acme.dto.CharacterRequest;
import org.acme.dto.SearchCharacterResponse;
import org.acme.entity.*;
import org.acme.entity.Character;
import org.acme.representation.CharacterRepresentation;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.acme.representation.TechniqueRepresentation;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/characters")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CharacterResource {

    @GET
    @Operation(
            summary = "Lista todos os personagens com paginação",
            description = "Retorna uma lista com todos os personagens do Jujutsu"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de personagens retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchCharacterResponse.class)
            )
    )
    public Response list(@Parameter(description = "Número da página (inicia em 1)")
                         @QueryParam("page") @DefaultValue("1") int page,

                         @Parameter(description = "Tamanho da página")
                         @QueryParam("size") @DefaultValue("10") int size,

                         @Parameter(description = "Campo para ordenação")
                         @QueryParam("sort") @DefaultValue("id") String sort,

                         @Parameter(description = "Direção da ordenação (asc/desc)")
                         @QueryParam("direction") @DefaultValue("asc") String direction
    ) {
        Set<String> allowed = Set.of("id", "name", "rank");
        if (!allowed.contains(sort)) sort = "id";

        Sort sortObj = Sort.by(
                sort,
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending
        );

        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Character> query = Character.findAll(sortObj);
        List<CharacterRepresentation> characters = query.page(effectivePage, size)
                .list()
                .stream()
                .map(CharacterRepresentation::from)
                .toList();

        SearchCharacterResponse response = new SearchCharacterResponse();
        response.characters = characters;
        response.totalCharacters = query.count();
        response.totalPages = query.pageCount();
        response.hasMore = (effectivePage + 1) < query.pageCount();
        response.nextPage = response.hasMore
                ? "/characters?page=" + (page + 1) + "&size=" + size + "&sort=" + sort + "&direction=" + direction
                : "";

        return Response.ok(response).build();
    }

    @GET
    @Path("{id}")
    @Operation(summary = "Busca personagem por ID",
            description = "Retorna um personagem específico pelo seu ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Personagem encontrado"),
            @APIResponse(responseCode = "404", description = "Personagem não encontrado")
    })
    public Response getById(@Parameter(description = "ID do personagem", required = true)
                            @PathParam("id") Long id) {
        Character entity = Character.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Character not found")).build();
        }
        return Response.ok(CharacterRepresentation.from(entity)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca personagens por nome",
            description = "Retorna uma lista com personagens buscados por nome"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de personagens encontrada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchCharacterResponse.class)
            )
    )
    public Response search(
            @Parameter(description = "Texto da pesquisa (nome do personagem)")
            @QueryParam("q") String q,

            @Parameter(description = "Campo para ordenação")
            @QueryParam("sort") @DefaultValue("id") String sort,

            @Parameter(description = "Direção da ordenação (asc/desc)")
            @QueryParam("direction") @DefaultValue("asc") String direction,

            @Parameter(description = "Número da página (inicia em 1)")
            @QueryParam("page") @DefaultValue("1") int page,

            @Parameter(description = "Tamanho da página")
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        Set<String> allowed = Set.of("id", "name", "rank");
        if (!allowed.contains(sort)) sort = "id";

        Sort sortObj = Sort.by(
                sort,
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending
        );

        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Character> query = (q == null || q.isBlank())
                ? Character.findAll(sortObj)
                : Character.find("lower(name) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        List<CharacterRepresentation> characters = query.page(effectivePage, size)
                .list()
                .stream()
                .map(CharacterRepresentation::from)
                .toList();

        SearchCharacterResponse response = new SearchCharacterResponse();
        response.characters = characters;
        response.totalCharacters = query.count();
        response.totalPages = query.pageCount();
        response.hasMore = (effectivePage + 1) < query.pageCount();
        response.nextPage = response.hasMore
                ? "/characters/search?q=" + q + "&page=" + (page + 1) + "&size=" + size + "&sort=" + sort + "&direction=" + direction
                : "";

        return Response.ok(response).build();
    }


    @POST
    @Operation(summary = "Cria um novo personagem",
            description = "Cria e persiste um personagem com as informações fornecidas"
    )
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Personagem criado com sucesso"),
            @APIResponse(responseCode = "400", description = "Dados inválidos")
    })
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CharacterRequest.class)
            )
    )
    @Transactional
    public Response create(@Valid CharacterRequest input, @Context UriInfo uriInfo) {
        if (input.name == null || input.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Character name is required")).build();
        }

        Character character = new Character();
        character.name = input.name;

        try {
            character.rank = input.rank != null ? Rank.valueOf(input.rank.toUpperCase()) : Rank.NON_SORCERER;
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Invalid rank: " + input.rank)).build();
        }

        // Clan
        if (input.clanName == null || input.clanName.isBlank()) {
            character.clan = Clan.find("lower(name)", "sem clã").firstResult();
        } else {
            Clan clan = Clan.find("lower(name)", input.clanName.toLowerCase()).firstResult();
            if (clan == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "clan '" + input.clanName + "' not found"))
                        .build();
            }
            character.clan = clan;
        }

        // Técnicas
        List<Technique> resolvedTechniques = new ArrayList<>();
        if (input.techniqueNames != null && !input.techniqueNames.isEmpty()) {
            for (String tName : input.techniqueNames) {
                Technique t = Technique.find("lower(name)", tName.toLowerCase()).firstResult();
                if (t == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "technique '" + tName + "' not found")).build();
                }
                if (resolvedTechniques.stream().noneMatch(x -> x.id.equals(t.id))) {
                    resolvedTechniques.add(t);
                }
            }
        }
        character.techniques = resolvedTechniques;

        // Domain Expansion
        DomainExpansion expansionToAssign = null;
        boolean expansionNeedPersist = false;
        if (input.domainExpansionName != null && !input.domainExpansionName.isBlank()) {
            DomainExpansion existing = DomainExpansion.find("lower(name)", input.domainExpansionName.toLowerCase()).firstResult();
            if (existing != null) {
                if (existing.owner != null) {
                    return Response.status(Response.Status.CONFLICT).entity(Map.of("error", "domain expansion '" + input.domainExpansionName + "' already assigned to character id " + existing.owner.id)).build();
                }
                expansionToAssign = existing;
            } else {
                expansionToAssign = new DomainExpansion(input.domainExpansionName, input.domainExpansionEffect, null);
                expansionNeedPersist = true;
            }
        }

        character.persist();

        if (expansionToAssign != null) {
            expansionToAssign.owner = character;
            if (expansionNeedPersist) expansionToAssign.persist();
            else expansionToAssign.persist();
            character.domainExpansion = expansionToAssign;
            character.persist();
        }

        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(character.id)).build();
        return Response.created(uri).entity(CharacterRepresentation.from(character)).build();
    }

    @PUT
    @Path("{id}")
    @Operation(summary = "Atualiza um personagem existente",
            description = "Atualiza os dados de um personagem pelo ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Personagem atualizado com sucesso"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "404", description = "Personagem não encontrado")
    })
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CharacterRequest.class)
            )
    )
    @Transactional
    public Response update(@Parameter(description = "ID do personagem", required = true)
                           @PathParam("id") Long id,
                           @Valid CharacterRequest input) {

        Character entity = Character.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Character not found")).build();
        }

        if (input.name == null || input.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Character name is required")).build();
        }
        entity.name = input.name;

        try {
            entity.rank = input.rank != null ? Rank.valueOf(input.rank.toUpperCase()) : Rank.NON_SORCERER;
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "Invalid rank: " + input.rank)).build();
        }

        // Clan
        if (input.clanName == null || input.clanName.isBlank()) {
            entity.clan = Clan.find("lower(name)", "sem clã").firstResult();
        } else {
            Clan clan = Clan.find("lower(name)", input.clanName.toLowerCase()).firstResult();
            if (clan == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "clan '" + input.clanName + "' not found"))
                        .build();
            }
            entity.clan = clan;
        }

        // Técnicas
        if (input.techniqueNames == null || input.techniqueNames.isEmpty()) {
            entity.techniques = new ArrayList<>();
        } else {
            List<Technique> resolvedTechniques = new ArrayList<>();
            for (String tName : input.techniqueNames) {
                if (tName == null || tName.isBlank()) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "each technique must have a valid name")).build();
                }
                Technique t = Technique.find("lower(name)", tName.toLowerCase()).firstResult();
                if (t == null) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "technique '" + tName + "' not found")).build();
                }
                if (resolvedTechniques.stream().noneMatch(x -> x.id.equals(t.id))) {
                    resolvedTechniques.add(t);
                }
            }
            entity.techniques = resolvedTechniques;
        }

        // Domain Expansion
        if (input.domainExpansionName == null || input.domainExpansionName.isBlank()) {
            if (entity.domainExpansion != null) {
                DomainExpansion old = entity.domainExpansion;
                old.owner = null;
                old.persist();
                entity.domainExpansion = null;
            }
        } else {
            DomainExpansion expansion = DomainExpansion.find("lower(name)", input.domainExpansionName.toLowerCase()).firstResult();
            if (expansion == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", "domain expansion '" + input.domainExpansionName + "' not found")).build();
            }
            if (expansion.owner != null && !expansion.owner.id.equals(entity.id)) {
                return Response.status(Response.Status.CONFLICT).entity(Map.of("error", "domain expansion '" + expansion.name + "' already assigned to character id " + expansion.owner.id)).build();
            }

            // Liberar a antiga se for diferente
            if (entity.domainExpansion != null && !entity.domainExpansion.id.equals(expansion.id)) {
                DomainExpansion old = entity.domainExpansion;
                old.owner = null;
                old.persist();
            }

            // Atualizar referência e efeito
            entity.domainExpansion = expansion;
            expansion.owner = entity;
            if (input.domainExpansionEffect != null && !input.domainExpansionEffect.isBlank()) {
                expansion.effect = input.domainExpansionEffect;
            }
        }

        return Response.ok(CharacterRepresentation.from(entity)).build();
    }

    @DELETE
    @Path("/{id}")
    @Operation(summary = "Remove um personagem",
            description = "Deleta um personagem pelo ID sem excluir a sua expansão de domínio"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Personagem removido com sucesso"),
            @APIResponse(responseCode = "404", description = "Personagem não encontrado")
    })
    @Transactional
    public Response delete(@Parameter(description = "ID do personagem", required = true)
                           @PathParam("id") Long id) {

        Character c = Character.findById(id);
        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Character not found")).build();
        }

        // Se o personagem tinha expansão, remover o vínculo antes
        if (c.domainExpansion != null) {
            c.domainExpansion.owner = null; // desvincula a expansão
            c.domainExpansion = null;       // limpa a referência no personagem
        }

        c.delete();
        return Response.noContent().build();
    }

    @GET
    @Path("/clan/{clanName}")
    @Operation(
            summary = "Lista personagens de um clã",
            description = "Retorna todos os personagens que pertencem ao clã informado, com suporte a paginação"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de personagens retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CharacterRepresentation.class, type = SchemaType.ARRAY)
            )
    )
    @APIResponse(responseCode = "404", description = "Clã não encontrado")
    public Response byClan(
            @Parameter(description = "Nome do clã a ser pesquisado", required = true)
            @PathParam("clanName") String clanName,

            @Parameter(description = "Número da página (inicia em 1)", example = "1")
            @QueryParam("page") @DefaultValue("1") int page,

            @Parameter(description = "Quantidade de registros por página", example = "10")
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        Clan clan = Clan.find("name", clanName).firstResult();
        if (clan == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "clan '" + clanName + "' not found")).build();
        }

        int effectivePage = page <= 1 ? 0 : page - 1;

        List<CharacterRepresentation> paged = clan.members.stream()
                .skip((long) effectivePage * size)
                .limit(size)
                .map(CharacterRepresentation::from)
                .toList();

        return Response.ok(paged).build();
    }

    @GET
    @Path("/rank/{rank}")
    @Operation(
            summary = "Lista personagens por rank",
            description = "Retorna todos os personagens do rank informado, com suporte a paginação"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de personagens retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CharacterRepresentation.class, type = SchemaType.ARRAY)
            )
    )
    @APIResponse(responseCode = "400", description = "Rank inválido")
    public Response byRank(
            @Parameter(description = "Rank do personagem (ex: SPECIAL_GRADE, GRADE_1)", required = true)
            @PathParam("rank") String rank,

            @Parameter(description = "Número da página (inicia em 1)", example = "1")
            @QueryParam("page") @DefaultValue("1") int page,

            @Parameter(description = "Quantidade de registros por página", example = "10")
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        try {
            Rank enumRank = Rank.valueOf(rank.toUpperCase());
            List<Character> list = Character.list("rank", enumRank);

            int effectivePage = page <= 1 ? 0 : page - 1;

            List<CharacterRepresentation> paged = list.stream()
                    .skip((long) effectivePage * size)
                    .limit(size)
                    .map(CharacterRepresentation::from)
                    .toList();

            return Response.ok(paged).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Invalid rank: " + rank)).build();
        }
    }

    @GET
    @Path("/{id}/techniques")
    @Operation(
            summary = "Lista técnicas de um personagem",
            description = "Retorna todas as técnicas associadas a um personagem, com suporte a paginação"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de técnicas retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TechniqueRepresentation.class, type = SchemaType.ARRAY)
            )
    )
    @APIResponse(responseCode = "404", description = "Personagem não encontrado")
    public Response techniques(
            @Parameter(description = "ID do personagem", required = true)
            @PathParam("id") Long id,

            @Parameter(description = "Número da página (inicia em 1)", example = "1")
            @QueryParam("page") @DefaultValue("1") int page,

            @Parameter(description = "Quantidade de registros por página", example = "10")
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        Character c = Character.findById(id);
        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Character not found")).build();
        }

        int effectivePage = page <= 1 ? 0 : page - 1;

        List<TechniqueRepresentation> paged = c.techniques.stream()
                .skip((long) effectivePage * size)
                .limit(size)
                .map(TechniqueRepresentation::from)
                .toList();

        return Response.ok(paged).build();
    }
}