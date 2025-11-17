package org.acme.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@Provider
@Priority(Priorities.HEADER_DECORATOR)
public class ApiVersionFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext ctx) {

        String version = ctx.getHeaderString("X-API-Version");

        if (version == null || version.isBlank()) {
            version = "1";
            ctx.getHeaders().add("X-API-Version", version);
        }

        ctx.setProperty("api-version", version);
    }
}
