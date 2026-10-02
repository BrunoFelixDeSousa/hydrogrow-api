package dev.brunofelix.tenancy.domain.valueobject;

import java.util.Locale;

public record Address(
    String street, String addressNumber, String city, String state, String zipCode) {
  public Address {

    state = state.toUpperCase(Locale.ROOT).trim();

    if (state.length() != 2) {
      throw new IllegalArgumentException("O estado deve ter exatamente 2 caracteres");
    }
  }
}
