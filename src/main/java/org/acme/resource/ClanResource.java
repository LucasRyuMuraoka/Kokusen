package org.acme.resource;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.acme.dto.ClanRequest;
import org.acme.dto.SearchClanResponse;
import org.acme.entity.Clan;
import org.acme.entity.Character;
import org.acme.representation.CharacterRepresentation;
import org.acme.representation.ClanRepresentation;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Path("/clans")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ClanResource {

    @GET
    @Operation(summary = "Lista todos os clãs com paginação",
            description = "Retorna uma lista paginada de clãs")
    @APIResponse(
            responseCode = "200",
            description = "Lista de clãs retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchClanResponse.class)
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
        Set<String> allowed = Set.of("id", "name");
        if (!allowed.contains(sort)) sort = "id";

        Sort sortObj = Sort.by(
                sort,
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending
        );

        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Clan> query = Clan.findAll(sortObj);
        List<ClanRepresentation> clans = query.page(effectivePage, size)
                .list()
                .stream()
                .map(ClanRepresentation::from)
                .toList();

        SearchClanResponse response = new SearchClanResponse();
        response.clans = clans;
        response.totalClans = query.count();
        response.totalPages = query.pageCount();
        response.hasMore = (effectivePage + 1) < query.pageCount();
        response.nextPage = response.hasMore
                ? "/clans?page=" + (page + 1) + "&size=" + size + "&sort=" + sort + "&direction=" + direction
                : "";

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca clã por ID",
            description = "Retorna um clã específico pelo seu ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Clã encontrado"),
            @APIResponse(responseCode = "404", description = "Clã não encontrado")
    })
    public Response getById(@Parameter(description = "ID do clã", required = true)
                            @PathParam("id") Long id) {

        Clan c = Clan.findById(id);
        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Clan not found")).build();
        }
        return Response.ok(ClanRepresentation.from(c)).build();
    }

    @GET
    @Path("/search")
    @Operation(summary = "Busca clãs por nome",
            description = "Retorna uma lista com clãs buscados por nome"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de clãs encontrada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchClanResponse.class)
            )
    )
    public Response search(
            @Parameter(description = "Texto da pesquisa (nome do clã)")
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
        Set<String> allowed = Set.of("id", "name");
        if (!allowed.contains(sort)) sort = "id";

        Sort sortObj = Sort.by(
                sort,
                "desc".equalsIgnoreCase(direction) ? Sort.Direction.Descending : Sort.Direction.Ascending
        );

        int effectivePage = page <= 1 ? 0 : page - 1;

        PanacheQuery<Clan> query = (q == null || q.isBlank())
                ? Clan.findAll(sortObj)
                : Clan.find("lower(name) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        List<ClanRepresentation> clans = query.page(effectivePage, size)
                .list()
                .stream()
                .map(ClanRepresentation::from)
                .toList();

        SearchClanResponse response = new SearchClanResponse();
        response.clans = clans;
        response.totalClans = query.count();
        response.totalPages = query.pageCount();
        response.hasMore = (effectivePage + 1) < query.pageCount();
        response.nextPage = response.hasMore
                ? "/clans/search?q=" + q + "&page=" + (page + 1) + "&size=" + size + "&sort=" + sort + "&direction=" + direction
                : "";

        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Cria um novo clã",
            description = "Cria e persiste um clã com as informações fornecidas"
    )
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Clã criado com sucesso"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "409", description = "Nome de clã já existente")
    })
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ClanRequest.class)
            )
    )
    public Response create(@Valid ClanRequest input, @Context UriInfo uriInfo) {
        if (input.name == null || input.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Clan name is required")).build();
        }

        Clan existing = Clan.find("lower(name)", input.name.toLowerCase()).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Clan with this name already exists")).build();
        }

        Clan entity = new Clan(input.name, input.description != null ? input.description : "");
        entity.persist();

        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(entity.id)).build();
        return Response.created(uri).entity(ClanRepresentation.from(entity)).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    @Operation(summary = "Atualiza um clã existente",
            description = "Atualiza os dados de um clã pelo ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Clã atualizado com sucesso"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "404", description = "Clã não encontrado"),
            @APIResponse(responseCode = "409", description = "Nome de clã já existente")
    })
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ClanRequest.class)
            )
    )
    public Response update(@Parameter(description = "ID do clã", required = true)
                           @PathParam("id") Long id,
                           @Valid ClanRequest input) {

        Clan entity = Clan.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).
                    entity(Map.of("error", "Clan not found")).build();
        }

        if (input.name == null || input.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Clan name is required")).build();
        }

        Clan existing = Clan.find("lower(name)", input.name.toLowerCase()).firstResult();
        if (existing != null && !existing.id.equals(id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Clan with this name already exists")).build();
        }

        entity.name = input.name;
        entity.description = input.description != null ? input.description : "";

        return Response.ok(ClanRepresentation.from(entity)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Remove um clã",
            description = "Deleta um clã pelo ID. Se o clã possuir membros, eles serão automaticamente atribuídos ao clã 'Sem Clã'."
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Clã removido com sucesso"),
            @APIResponse(responseCode = "404", description = "Clã não encontrado"),
            @APIResponse(responseCode = "500", description = "Clã 'Sem Clã' não encontrado no sistema")
    })
    public Response delete(@Parameter(description = "ID do clã", required = true)
                           @PathParam("id") Long id) {

        Clan entity = Clan.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Clan not found")).build();
        }

        Clan semClan = Clan.find("lower(name)", "sem clã").firstResult();
        if (semClan == null) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Default clan 'Sem Clã' not found in the system"))
                    .build();
        }

        if (entity.members != null && !entity.members.isEmpty()) {
            for (Character member : entity.members) {
                member.clan = semClan;
                member.persist();
            }
        }

        entity.delete();
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/members")
    @Operation(summary = "Lista membros de um clã",
            description = "Retorna os personagens de um clã específico, com paginação"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de membros retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CharacterRepresentation.class, type = SchemaType.ARRAY)
            )
    )
    @APIResponse(responseCode = "404", description = "Clã não encontrado")
    public Response members(
            @Parameter(description = "ID do clã", required = true)
            @PathParam("id") Long id,

            @Parameter(description = "Número da página (inicia em 1)")
            @QueryParam("page") @DefaultValue("1") int page,

            @Parameter(description = "Quantidade de registros por página")
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        Clan c = Clan.findById(id);
        if (c == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Clan not found")).build();
        }

        int effectivePage = page <= 1 ? 0 : page - 1;

        List<CharacterRepresentation> paged = c.members.stream()
                .skip((long) effectivePage * size)
                .limit(size)
                .map(CharacterRepresentation::from)
                .toList();

        return Response.ok(paged).build();
    }
}