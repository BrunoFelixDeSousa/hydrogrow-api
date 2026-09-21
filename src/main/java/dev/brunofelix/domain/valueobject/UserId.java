package dev.brunofelix.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) {
    
    public UserId {
        Objects.requireNonNull(value, "O UserId não pode ser nulo");
    }

    public static UserId of (final UUID value) {
        return new UserId(value);
    }

    public static UserId of(final String value) {
        Objects.requireNonNull(value, "O UserId não pode ser nulo");
        return new UserId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }

    
}