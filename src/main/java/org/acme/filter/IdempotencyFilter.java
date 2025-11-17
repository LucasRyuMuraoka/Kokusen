package org.acme.filter;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.ext.Provider;
import org.acme.idempotency.IdempotencyService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Provider
@Priority(Priorities.USER)
public class IdempotencyFilter implements ContainerRequestFilter, ContainerResponseFilter {

    @Inject
    IdempotencyService idempotencyService;

    private static final String KEY_PROPERTY = "IDEMPOTENCY_KEY_GENERATED";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        if (!"POST".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        InputStream in = requestContext.getEntityStream();
        byte[] bodyBytes = in.readAllBytes();
        String bodyString = new String(bodyBytes);

        requestContext.setEntityStream(new ByteArrayInputStream(bodyBytes));

        String key = idempotencyService.generateKeyFromBody(bodyString);

        idempotencyService.checkOrThrow(key);

        requestContext.setProperty(KEY_PROPERTY, key);
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) {

        if (!"POST".equalsIgnoreCase(requestContext.getMethod())) {
            return;
        }

        Object keyObj = requestContext.getProperty(KEY_PROPERTY);
        if (keyObj == null) return;

        String key = keyObj.toString();

        Object responseEntity = responseContext.getEntity();
        if (responseEntity != null) {
            idempotencyService.saveResponse(key, responseEntity);
        }
    }
}