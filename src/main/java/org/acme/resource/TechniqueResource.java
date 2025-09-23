package org.acme.resource;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import jakarta.validation.Valid;
import org.acme.dto.SearchTechniqueResponse;
import org.acme.dto.TechniqueRequest;
import org.acme.entity.Technique;
import org.acme.representation.CharacterRepresentation;
import org.acme.representation.TechniqueRepresentation;

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

@Path("/techniques")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TechniqueResource {

    @GET
    @Operation(summary = "Lista todas as técnicas com paginação",
            description = "Retorna uma lista paginada de técnicas")
    @APIResponse(
            responseCode = "200",
            description = "Lista de técnicas retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchTechniqueResponse.class)
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

        PanacheQuery<Technique> query = Technique.findAll(sortObj);
        List<TechniqueRepresentation> techniques = query.page(effectivePage, size)
                .list()
                .stream()
                .map(TechniqueRepresentation::from)
                .toList();

        SearchTechniqueResponse response = new SearchTechniqueResponse();
        response.techniques = techniques;
        response.totalTechniques = query.count();
        response.totalPages = query.pageCount();
        response.hasMore = (effectivePage + 1) < query.pageCount();
        response.nextPage = response.hasMore
                ? "/techniques?page=" + (page + 1) + "&size=" + size + "&sort=" + sort + "&direction=" + direction
                : "";

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca técnica por ID",
            description = "Retorna uma técnica específica pelo seu ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Técnica encontrada"),
            @APIResponse(responseCode = "404", description = "Técnica não encontrada")
    })
    public Response getById(@Parameter(description = "ID da técnica", required = true)
                            @PathParam("id") Long id) {

        Technique t = Technique.findById(id);
        if (t == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Technique not found")).build();
        }
        return Response.ok(TechniqueRepresentation.from(t)).build();
    }

    @GET
    @Path("/search")
    @Operation(
            summary = "Busca técnicas por nome",
            description = "Retorna uma lista com técnicas buscadas por nome"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de técnicas encontrada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchTechniqueResponse.class)
            )
    )
    public Response search(
            @Parameter(description = "Texto da pesquisa (nome da técnica)")
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

        PanacheQuery<Technique> query = (q == null || q.isBlank())
                ? Technique.findAll(sortObj)
                : Technique.find("lower(name) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        List<TechniqueRepresentation> techniques = query.page(effectivePage, size)
                .list()
                .stream()
                .map(TechniqueRepresentation::from)
                .toList();

        SearchTechniqueResponse response = new SearchTechniqueResponse();
        response.techniques = techniques;
        response.totalTechniques = query.count();
        response.totalPages = query.pageCount();
        response.hasMore = (effectivePage + 1) < query.pageCount();
        response.nextPage = response.hasMore
                ? "/techniques/search?q=" + (q != null ? q : "") + "&page=" + (page + 1)
                + "&size=" + size + "&sort=" + sort + "&direction=" + direction
                : "";

        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Cria uma nova técnica",
            description = "Cria e persiste uma técnica com as informações fornecidas"
    )
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Técnica criada com sucesso"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "409", description = "Nome de técnica já existente")
    })
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TechniqueRequest.class)
            )
    )
    public Response create(@Valid TechniqueRequest input, @Context UriInfo uriInfo) {
        if (input.name == null || input.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Technique name is required")).build();
        }

        Technique existing = Technique.find("lower(name)", input.name.toLowerCase()).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Technique with this name already exists")).build();
        }

        Technique entity = new Technique(input.name, input.description != null ? input.description : "");
        entity.persist();

        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(entity.id)).build();
        return Response.created(uri).entity(TechniqueRepresentation.from(entity)).build();
    }

    @PUT
    @Path("{id}")
    @Transactional
    @Operation(summary = "Atualiza uma técnica existente",
            description = "Atualiza os dados de uma técnica pelo ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Técnica atualizada com sucesso"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "404", description = "Técnica não encontrada"),
            @APIResponse(responseCode = "409", description = "Nome de técnica já existente")
    })
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TechniqueRequest.class)
            )
    )
    public Response update(@Parameter(description = "ID da técnica", required = true)
                           @PathParam("id") Long id,
                           @Valid TechniqueRequest input) {

        Technique entity = Technique.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Technique not found")).build();
        }

        if (input.name == null || input.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Technique name is required")).build();
        }

        Technique existing = Technique.find("lower(name)", input.name.toLowerCase()).firstResult();
        if (existing != null && !existing.id.equals(id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Technique with this name already exists")).build();
        }

        entity.name = input.name;
        entity.description = input.description != null ? input.description : "";

        return Response.ok(TechniqueRepresentation.from(entity)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Remove uma técnica",
            description = "Deleta uma técnica pelo ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Técnica removida com sucesso"),
            @APIResponse(responseCode = "404", description = "Técnica não encontrada")
    })
    public Response delete(@Parameter(description = "ID da técnica", required = true)
                           @PathParam("id") Long id) {

        Technique entity = Technique.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", "Technique not found")).build();
        }

        // limpar associações
        entity.users.forEach(u -> u.techniques.remove(entity));
        entity.users.clear();

        entity.delete();
        return Response.noContent().build();
    }

    @GET
    @Path("/{id}/users")
    @Operation(summary = "Lista usuários de uma técnica",
            description = "Retorna os personagens que possuem essa técnica, com paginação"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de personagens retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CharacterRepresentation.class, type = SchemaType.ARRAY)
            )
    )
    @APIResponse(responseCode = "404", description = "Técnica não encontrada")
    public Response users(
            @Parameter(description = "ID da técnica", required = true)
            @PathParam("id") Long id,

            @Parameter(description = "Número da página (inicia em 1)")
            @QueryParam("page") @DefaultValue("1") int page,

            @Parameter(description = "Quantidade de registros por página")
            @QueryParam("size") @DefaultValue("10") int size
    ) {
        Technique t = Technique.findById(id);
        if (t == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Technique not found")).build();
        }

        int effectivePage = page <= 1 ? 0 : page - 1;

        List<CharacterRepresentation> users = t.users.stream()
                .skip((long) effectivePage * size)
                .limit(size)
                .map(CharacterRepresentation::from)
                .toList();

        return Response.ok(users).build();
    }
}
