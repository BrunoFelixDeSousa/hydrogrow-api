package dev.brunofelix.tenancy.application.port;

import dev.brunofelix.tenancy.domain.entity.Tenant;
import java.util.Optional;
import java.util.UUID;

public interface TenantStore {

  Optional<Tenant> find(final UUID id);

  void insert(final Tenant tenant);
}
