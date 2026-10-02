package dev.brunofelix.tenancy.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TenantId(UUID value) {

  public TenantId {
    Objects.requireNonNull(value, "O TenantId não pode ser nulo");
  }

  public static TenantId of(final UUID value) {
    return new TenantId(value);
  }

  public static TenantId of(final String value) {
    Objects.requireNonNull(value, "O TenantId não pode ser nulo");
    return new TenantId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
