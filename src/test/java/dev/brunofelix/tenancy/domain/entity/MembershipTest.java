package dev.brunofelix.tenancy.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class MembershipTest {

  @Test
  void shouldUseTenantAndUserAsIdentity() {
    UUID tenantId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
    Membership membership = Membership.create(tenantId, userId);

    assertThat(membership.tenantId()).isEqualTo(tenantId);
    assertThat(membership.userId()).isEqualTo(userId);
    assertThat(membership.isActive()).isTrue();

    membership.deactivate();

    assertThat(membership.isActive()).isFalse();
  }
}
