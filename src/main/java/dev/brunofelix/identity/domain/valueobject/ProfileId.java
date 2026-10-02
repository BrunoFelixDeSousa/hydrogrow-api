package dev.brunofelix.identity.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record ProfileId(UUID value) {

  public ProfileId {
    Objects.requireNonNull(value, "O ProfileId não pode ser nulo");
  }

  public static ProfileId of(final UUID value) {
    return new ProfileId(value);
  }

  public static ProfileId of(final String value) {
    Objects.requireNonNull(value, "O ProfileId não pode ser nulo");
    return new ProfileId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
