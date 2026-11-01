package dev.brunofelix.tenancy.domain.entity;

import java.util.Objects;
import java.util.UUID;

public final class Membership {

  private final UUID tenantId;
  private final UUID userId;
  private boolean active;

  private Membership(final UUID tenantId, final UUID userId, final boolean active) {
    this.tenantId = Objects.requireNonNull(tenantId, "O identificador do tenant é obrigatório");
    this.userId = Objects.requireNonNull(userId, "O identificador do usuário é obrigatório");
    this.active = active;
  }

  public static Membership create(final UUID tenantId, final UUID userId) {
    return new Membership(tenantId, userId, true);
  }

  public static Membership restore(final UUID tenantId, final UUID userId, final boolean active) {
    return new Membership(tenantId, userId, active);
  }

  public void activate() {
    active = true;
  }

  public void deactivate() {
    active = false;
  }

  public UUID tenantId() {
    return tenantId;
  }

  public UUID userId() {
    return userId;
  }

  public boolean isActive() {
    return active;
  }
}
