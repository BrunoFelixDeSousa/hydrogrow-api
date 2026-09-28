package dev.brunofelix.identity.domain.valueobject;

import java.time.Instant;
import java.util.Objects;

public record UserTimestamps(Instant lastLoginAt, Instant createdAt, Instant updatedAt) {

    public UserTimestamps {
        Objects.requireNonNull(createdAt, "A data de criação não pode ser nula");
        Objects.requireNonNull(updatedAt, "A data de atualização não pode ser nula");
    }

    public static UserTimestamps startingAt(Instant now) {
        return new UserTimestamps(null, now, now);
    }
}
