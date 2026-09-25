package dev.brunofelix.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("UserStatus")
class UserStatusTest {

    @Nested
    @DisplayName("When checking authentication")
    class WhenCheckingAuthentication {

        @ParameterizedTest(name = "[{index}] {0} should be allowed to authenticate")
        @EnumSource(value = UserStatus.class, names = {"ACTIVE"})
        void shouldAllowAuthentication(UserStatus status) {
            // When
            boolean canAuthenticate = status.canAuthenticate();

            // Then
            assertThat(canAuthenticate).isTrue();
        }

        @ParameterizedTest(name = "[{index}] {0} should not be allowed to authenticate")
        @EnumSource(value = UserStatus.class, names = {"PENDING", "BLOCKED", "DISABLED"})
        void shouldNotAllowAuthentication(UserStatus status) {
            // When
            boolean canAuthenticate = status.canAuthenticate();

            // Then
            assertThat(canAuthenticate).isFalse();
        }
    }

    @Nested
    @DisplayName("When checking blocked status")
    class WhenCheckingBlockedStatus{

        @ParameterizedTest(name = "[{index}] {0} should be blocked")
        @EnumSource(value = UserStatus.class, names = {"BLOCKED"})
        void shouldBeBlocked(UserStatus status) {
            // When
            boolean blocked = status.isBlocked();

            // Then
            assertThat(blocked).isTrue();
        }

        @ParameterizedTest(name = "[{index}] {0} should not be blocked")
        @EnumSource(value = UserStatus.class, names = {"PENDING", "ACTIVE", "DISABLED"})
        void shouldNotBeBlocked(UserStatus status) {
            // When
            boolean blocked = status.isBlocked();

            // Then
            assertThat(blocked).isFalse();
        }
    }

}