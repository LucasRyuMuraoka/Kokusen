package org.acme.filter;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Provider
@Priority(Priorities.AUTHENTICATION)
public class RateLimitFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final int LIMIT = 18;
    private static final int WINDOW_SECONDS = 30;
    private static final Map<String, RequestInfo> requestCounts = new ConcurrentHashMap<>();

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String ip = requestContext.getHeaderString("X-Forwarded-For");
        if (ip == null) ip = requestContext.getUriInfo().getRequestUri().getHost();

        long now = Instant.now().getEpochSecond();
        RequestInfo info = requestCounts.computeIfAbsent(ip, k -> new RequestInfo());

        if (now - info.timestamp > WINDOW_SECONDS) {
            info.timestamp = now;
            info.count = 0;
        }

        info.count++;
        requestContext.setProperty("rate-info", info);

        if (info.count > LIMIT) {
            Response response = Response.status(Response.Status.TOO_MANY_REQUESTS)
                    .entity(Map.of("error", "Too many requests — please try again later.",
                            "X-RateLimit-Limit", LIMIT,
                            "X-RateLimit-Remaining", 0))
                    .build();
            requestContext.abortWith(response);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        Object infoObj = requestContext.getProperty("rate-info");
        if (infoObj instanceof RequestInfo info) {
            responseContext.getHeaders().add("X-RateLimit-Limit", String.valueOf(LIMIT));
            responseContext.getHeaders().add("X-RateLimit-Remaining", String.valueOf(Math.max(0, LIMIT - info.count)));
        }
    }

    private static class RequestInfo {
        long timestamp = Instant.now().getEpochSecond();
        int count = 0;
    }
}
