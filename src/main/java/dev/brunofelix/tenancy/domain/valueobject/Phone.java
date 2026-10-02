package dev.brunofelix.tenancy.domain.valueobject;

public record Phone(String value) {

  public Phone {
    value = value.trim();

    if (value.length() != 11) {
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
