package dev.brunofelix.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;


@DisplayName("UserId")
class UserIdTest {
    
    @Nested
    @DisplayName("When creating from UUID")
    class WhenCreatingFromUUID {

        @Test
        @DisplayName("Should create a UserId from a UUID")
        void shouldCreateUserIdFromUUID() {
            // Given
            UUID id = UUID.randomUUID();

            // When
            UserId userId = UserId.of(id);

            // Then
            assertThat(userId.value()).isEqualTo(id);
        }

        @Test
        @DisplayName("Should reject null UUID")
        void shouldRejectNullUUID() {
            // When & Then
            assertThatNullPointerException().isThrownBy(() -> UserId.of((UUID) null))
                    .withMessage("O UserId não pode ser nulo");
        }
    }

    @Nested
    @DisplayName("When creating from String")
    class WhenCreatingFromString {
        
        @Test
        @DisplayName("Should create a UserId from a valid UUID string")
        void shouldCreateUserIdFromValidUUIDString() {
            // Given
            // String idString = UUID.randomUUID().toString();
            String value = "550e8400-e29b-41d4-a716-446655440000";

            // When
            UserId userId = UserId.of(value);

            // Then
            assertThat(userId.value()).isEqualTo(UUID.fromString(value));
        }

        @Test
        @DisplayName("should reject invalid uuid string")
        void shouldRejectInvalidUUIDString() {
            // Given
            String invalidIdString = "invalid-uuid";

            // When & Then
            assertThatIllegalArgumentException().isThrownBy(() -> UserId.of(invalidIdString));
        }
    }
}