package dev.brunofelix.tenancy.domain.valueobject;

import java.util.Objects;
import java.util.UUID;

public record TenantMembershipId(UUID value) {

  public TenantMembershipId {
    Objects.requireNonNull(value, "O TenantMembershipId não pode ser nulo");
  }

  public static TenantMembershipId of(final UUID value) {
    return new TenantMembershipId(value);
  }

  public static TenantMembershipId of(final String value) {
    Objects.requireNonNull(value, "O TenantMembershipId não pode ser nulo");
    return new TenantMembershipId(UUID.fromString(value));
  }

  @Override
  public String toString() {
    return value.toString();
  }
}
