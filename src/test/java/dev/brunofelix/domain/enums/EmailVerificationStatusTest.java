package dev.brunofelix.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@DisplayName("EmailVerificationStatus")
class EmailVerificationStatusTest {

     @Nested
    @DisplayName("When checking verification")
    class WhenCheckingVerification {

        @ParameterizedTest(name = "[{index}] {0} should be verified")
        @EnumSource(value = EmailVerificationStatus.class, names = {"VERIFIED"})
        void shouldBeVerified(EmailVerificationStatus status) {
            // When
            boolean verified = status.isVerified();

            // Then
            assertThat(verified).isTrue();
        }

        @ParameterizedTest(name = "[{index}] {0} should not be verified")
        @EnumSource(value = EmailVerificationStatus.class, names = {"PENDING"})
        void shouldNotBeVerified(EmailVerificationStatus status) {
            // When
            boolean verified = status.isVerified();

            // Then
            assertThat(verified).isFalse();
        }
    }

}