package dev.brunofelix.tenancy.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.brunofelix.shared.domain.valueobject.Name;
import dev.brunofelix.tenancy.domain.enums.Plan;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class TenantTest {

  private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");

  @Test
  void shouldCreateActiveTenantWithInactiveSubscription() {
    Tenant tenant = Tenant.create(TENANT_ID, Name.of("Hydro Grow"), Plan.BASIC);

    assertThat(tenant.id()).isEqualTo(TENANT_ID);
    assertThat(tenant.plan()).isEqualTo(Plan.BASIC);
    assertThat(tenant.isActive()).isTrue();
    assertThat(tenant.isSubscriptionActive()).isFalse();
  }

  @Test
  void shouldDeactivateTenantAndSubscriptionTogether() {
    Tenant tenant = Tenant.create(TENANT_ID, Name.of("Hydro Grow"), Plan.BASIC);
    tenant.activateSubscription();

    tenant.deactivate();

    assertThat(tenant.isActive()).isFalse();
    assertThat(tenant.isSubscriptionActive()).isFalse();
  }

  @Test
  void shouldRejectSubscriptionActivationForInactiveTenant() {
    Tenant tenant = Tenant.create(TENANT_ID, Name.of("Hydro Grow"), Plan.BASIC);
    tenant.deactivate();

    assertThatThrownBy(tenant::activateSubscription)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Um tenant inativo não pode ativar a assinatura");
  }
}
