package dev.brunofelix.tenancy.application.port;

import dev.brunofelix.identity.domain.valueobject.UserId;
import dev.brunofelix.tenancy.domain.entity.TenantMemberShip;
import dev.brunofelix.tenancy.domain.valueobject.TenantId;

public interface TenantMemberShipStore {

  boolean exists(final TenantId tenantId, final UserId userId);

  void insert(final TenantMemberShip tenantMemberShip);
}
