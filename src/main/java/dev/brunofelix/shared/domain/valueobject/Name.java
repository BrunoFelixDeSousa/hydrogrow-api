package dev.brunofelix.shared.domain.valueobject;

import java.util.Objects;

public record Name(String value) {

  public Name {
    Objects.requireNonNull(value, "O nome não pode ser nulo");
    value = value.trim();

    if (value.isBlank()) {
      throw new IllegalArgumentException("O nome não pode ser vazio");
    }

    if (value.length() < 2 || value.length() > 100) {
      throw new IllegalArgumentException("O nome deve ter entre 2 e 100 caracteres");
    }
  }

  public static Name of(final String value) {
    return new Name(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
