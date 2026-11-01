package dev.brunofelix.tenancy.domain.valueobject;

import java.util.Locale;

public record Address(
    String street, String addressNumber, String city, String state, String zipCode) {
  public Address {
    street = normalizeOptional(street);
    addressNumber = normalizeOptional(addressNumber);
    city = normalizeOptional(city);
    state = normalizeOptional(state);
    zipCode = normalizeOptional(zipCode);

    if (state != null) {
      state = state.toUpperCase(Locale.ROOT);
    }

    if (state != null && !state.matches("[A-Z]{2}")) {
      throw new IllegalArgumentException("O estado deve ter exatamente 2 caracteres");
    }
  }

  private static String normalizeOptional(final String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }
}
