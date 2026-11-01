package dev.brunofelix.tenancy.application.port;

import dev.brunofelix.tenancy.domain.entity.Membership;
import java.util.UUID;

public interface MembershipStore {

  boolean exists(final UUID tenantId, final UUID userId);

  void insert(final Membership membership);
}
