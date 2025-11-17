package org.acme.idempotency;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class IdempotencyStore {

    private final Map<String, Object> responses = new ConcurrentHashMap<>();

    public boolean contains(String key) {
        return responses.containsKey(key);
    }

    public Object get(String key) {
        return responses.get(key);
    }

    public void save(String key, Object response) {
        responses.put(key, response);
    }
}
