package dev.brunofelix.tenancy.application.port;

import dev.brunofelix.tenancy.domain.entity.Tenant;
import dev.brunofelix.tenancy.domain.valueobject.TenantId;
import java.util.Optional;

public interface TenantStore {

  Optional<Tenant> find(final TenantId id);

  void insert(final Tenant tenant);
}
