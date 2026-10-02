package dev.brunofelix.tenancy.domain.entity;

import dev.brunofelix.identity.domain.valueobject.UserId;
import dev.brunofelix.shared.domain.EntityValidation;
import dev.brunofelix.tenancy.domain.valueobject.TenantId;
import dev.brunofelix.tenancy.domain.valueobject.TenantMembershipId;
import java.time.Instant;

public record TenantMemberShip(
    TenantMembershipId id,
    TenantId tenantId,
    UserId userId,
    boolean active,
    Instant createdAt,
    Instant updatedAt) {

  public TenantMemberShip {
    id = EntityValidation.required(id, "O identificador da associação do tenant é obrigatório");

    tenantId = EntityValidation.required(tenantId, "O identificador do tenant é obrigatório");

    userId = EntityValidation.required(userId, "O identificador do usuário é obrigatório");

    createdAt =
        EntityValidation.required(
            createdAt, "A data de criação da associação do tenant é obrigatória");

    updatedAt =
        EntityValidation.required(
            updatedAt, "A data de atualização da associação do tenant é obrigatória");
  }

  public static TenantMemberShip create(
      final TenantMembershipId id,
      final TenantId tenantId,
      final UserId userId,
      final Instant createdAt) {

    return new TenantMemberShip(id, tenantId, userId, true, createdAt, createdAt);
  }
}
