package org.acme.resource;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Sort;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.acme.dto.DomainExpansionRequest;
import org.acme.dto.SearchDomainExpansionResponse;
import org.acme.entity.DomainExpansion;
import org.acme.representation.DomainExpansionRepresentation;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
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

@Path("/domain-expansions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DomainExpansionResource {

    @GET
    @Operation(summary = "Lista todas as expansões de domínio com paginação",
            description = "Retorna uma lista paginada de expansões")
    @APIResponse(
            responseCode = "200",
            description = "Lista de expansões de domínio retornada com sucesso",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchDomainExpansionResponse.class)
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

        PanacheQuery<DomainExpansion> query = DomainExpansion.findAll(sortObj);
        List<DomainExpansionRepresentation> items = query.page(effectivePage, size)
                .list()
                .stream()
                .map(DomainExpansionRepresentation::from)
                .toList();

        SearchDomainExpansionResponse response = new SearchDomainExpansionResponse();
        response.domainExpansions = items;
        response.totalDomainExpansions = query.count();
        response.totalPages = query.pageCount();
        response.hasMore = (effectivePage + 1) < query.pageCount();
        response.nextPage = response.hasMore
                ? "/domain-expansions?page=" + (page + 1) + "&size=" + size + "&sort=" + sort + "&direction=" + direction
                : "";

        return Response.ok(response).build();
    }

    @GET
    @Path("/{id}")
    @Operation(summary = "Busca uma expansão de domínio pelo ID",
            description = "Retorna uma expansão específica pelo seu ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Expansão encontrada"),
            @APIResponse(responseCode = "404", description = "Expansão não encontrada")
    })
    public Response getById(@Parameter(description = "ID da expansão de domínio", required = true)
                            @PathParam("id") Long id) {

        DomainExpansion d = DomainExpansion.findById(id);
        if (d == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "DomainExpansion not found")).build();
        }
        return Response.ok(DomainExpansionRepresentation.from(d)).build();
    }

    @GET
    @Path("/search")
    @Operation(
            summary = "Busca expansões de domínio por nome",
            description = "Retorna uma lista com expansões buscadas por nome"
    )
    @APIResponse(
            responseCode = "200",
            description = "Lista de expansões encontrada",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = SearchDomainExpansionResponse.class)
            )
    )
    public Response search(
            @Parameter(description = "Texto da pesquisa (nome da expansão)")
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

        PanacheQuery<DomainExpansion> query = (q == null || q.isBlank())
                ? DomainExpansion.findAll(sortObj)
                : DomainExpansion.find("lower(name) like ?1", sortObj, "%" + q.toLowerCase() + "%");

        List<DomainExpansionRepresentation> items = query.page(effectivePage, size)
                .list()
                .stream()
                .map(DomainExpansionRepresentation::from)
                .toList();

        SearchDomainExpansionResponse response = new SearchDomainExpansionResponse();
        response.domainExpansions = items;
        response.totalDomainExpansions = query.count();
        response.totalPages = query.pageCount();
        response.hasMore = (effectivePage + 1) < query.pageCount();
        response.nextPage = response.hasMore
                ? "/domain-expansions/search?q=" + q + "&page=" + (page + 1) + "&size=" + size + "&sort=" + sort + "&direction=" + direction
                : "";

        return Response.ok(response).build();
    }

    @POST
    @Transactional
    @Operation(summary = "Cria uma nova expansão de domínio",
            description = "Cria e persiste uma expansão com as informações fornecidas"
    )
    @APIResponses({
            @APIResponse(responseCode = "201", description = "Expansão criada com sucesso"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "409", description = "Nome já existente")
    })
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DomainExpansionRequest.class))
    )
    public Response create(@Valid DomainExpansionRequest input, @Context UriInfo uriInfo) {
        if (input.name == null || input.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "DomainExpansion name is required")).build();
        }

        DomainExpansion existing = DomainExpansion.find("lower(name)", input.name.toLowerCase()).firstResult();
        if (existing != null) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "DomainExpansion with this name already exists")).build();
        }

        DomainExpansion entity = new DomainExpansion(input.name, input.effect != null ? input.effect : "", null);
        entity.persist();

        URI uri = uriInfo.getAbsolutePathBuilder().path(String.valueOf(entity.id)).build();
        return Response.created(uri).entity(DomainExpansionRepresentation.from(entity)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Atualiza uma expansão de domínio existente",
            description = "Atualiza os dados de uma expansão pelo ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "200", description = "Expansão atualizada com sucesso"),
            @APIResponse(responseCode = "400", description = "Dados inválidos"),
            @APIResponse(responseCode = "404", description = "Expansão não encontrada"),
            @APIResponse(responseCode = "409", description = "Nome já existente")
    })
    @RequestBody(
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = DomainExpansionRequest.class)
            )
    )
    public Response update(@Parameter(description = "ID da expansão de domínio", required = true)
                           @PathParam("id") Long id,
                           @Valid DomainExpansionRequest input) {

        DomainExpansion entity = DomainExpansion.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "DomainExpansion not found")).build();
        }

        if (input.name == null || input.name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "DomainExpansion name is required")).build();
        }

        DomainExpansion existing = DomainExpansion.find("lower(name)", input.name.toLowerCase()).firstResult();
        if (existing != null && !existing.id.equals(id)) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "DomainExpansion with this name already exists")).build();
        }

        entity.name = input.name;
        entity.effect = input.effect != null ? input.effect : "";

        return Response.ok(DomainExpansionRepresentation.from(entity)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @Operation(summary = "Remove uma expansão de domínio",
            description = "Deleta uma expansão pelo ID"
    )
    @APIResponses({
            @APIResponse(responseCode = "204", description = "Expansão removida com sucesso"),
            @APIResponse(responseCode = "404", description = "Expansão não encontrada")
    })
    public Response delete(@Parameter(description = "ID da expansão de domínio", required = true)
                           @PathParam("id") Long id) {

        DomainExpansion entity = DomainExpansion.findById(id);
        if (entity == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "DomainExpansion not found")).build();
        }

        // limpa a associação
        if (entity.owner != null) {
            entity.owner.domainExpansion = null;
            entity.owner.persist();
            entity.owner = null;
        }

        entity.delete();
        return Response.noContent().build();
    }
}