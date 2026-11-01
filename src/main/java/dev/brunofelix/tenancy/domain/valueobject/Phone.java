package dev.brunofelix.tenancy.domain.valueobject;

import java.util.Objects;

public record Phone(String value) {

  public Phone {
    Objects.requireNonNull(value, "O telefone não pode ser nulo");
    value = value.trim();

    if (!value.matches("[0-9]{11}")) {
      throw new IllegalArgumentException(
          "O telefone deve conter apenas 11 dígitos, incluindo o DDD");
    }
  }

  public static Phone of(final String value) {
    return new Phone(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
