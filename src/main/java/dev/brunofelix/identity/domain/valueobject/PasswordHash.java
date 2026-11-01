package dev.brunofelix.identity.domain.valueobject;

import java.util.Objects;

public record PasswordHash(String value) {

  public PasswordHash {
    Objects.requireNonNull(value, "O hash da senha não pode ser nulo");
    value = value.trim();

    if (value.isBlank()) {
      throw new IllegalArgumentException("O hash da senha não pode ser vazio");
    }
  }

  public static PasswordHash of(final String value) {
    return new PasswordHash(value);
  }

  @Override
  public String toString() {
    return "********";
  }
}
