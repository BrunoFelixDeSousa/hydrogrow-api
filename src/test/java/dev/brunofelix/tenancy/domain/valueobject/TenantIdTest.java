package dev.brunofelix.tenancy.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("TenantId")
class TenantIdTest {

  @Nested
  @DisplayName("When creating from UUID")
  class WhenCreatingFromUUID {

    @Test
    @DisplayName("Should create a TenantId from a UUID")
    void shouldCreateTenantIdFromUUID() {
      // Given
      UUID id = UUID.randomUUID();

      // When
      TenantId tenantId = TenantId.of(id);

      // Then
      assertThat(tenantId.value()).isEqualTo(id);
    }

    @Test
    @DisplayName("Should reject null UUID")
    void shouldRejectNullUUID() {
      // When & Then
      assertThatNullPointerException()
          .isThrownBy(() -> TenantId.of((UUID) null))
          .withMessage("O TenantId não pode ser nulo");
    }
  }

  @Nested
  @DisplayName("When creating from String")
  class WhenCreatingFromString {

    @Test
    @DisplayName("Should create a TenantId from a valid UUID string")
    void shouldCreateTenantIdFromValidUUIDString() {
      // Given
      String value = "550e8400-e29b-41d4-a716-446655440000";

      // When
      TenantId tenantId = TenantId.of(value);

      // Then
      assertThat(tenantId.value()).isEqualTo(UUID.fromString(value));
    }

    @Test
    @DisplayName("should reject invalid UUID string")
    void shouldRejectInvalidUUIDString() {
      // Given
      String invalidIdString = "invalid-uuid";

      // When & Then
      assertThatIllegalArgumentException().isThrownBy(() -> TenantId.of(invalidIdString));
    }
  }
}
