package org.acme.idempotency;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@ApplicationScoped
public class IdempotencyService {

    @Inject
    IdempotencyStore store;

    public String generateKeyFromBody(String body) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(body.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to generate idempotency hash", e);
        }
    }

    public void checkOrThrow(String key) {
        if (store.contains(key)) {
            throw new WebApplicationException(
                    Response.status(Response.Status.CONFLICT)
                            .entity(store.get(key))
                            .build()
            );
        }
    }

    public void saveResponse(String key, Object response) {
        store.save(key, response);
    }
}
