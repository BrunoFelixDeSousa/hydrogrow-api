package dev.brunofelix.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import dev.brunofelix.domain.enums.EmailVerificationStatus;
import dev.brunofelix.domain.enums.UserStatus;
import dev.brunofelix.domain.exception.EmailAlreadyVerifiedException;
import dev.brunofelix.domain.valueobject.Email;
import dev.brunofelix.domain.valueobject.Password;
import dev.brunofelix.domain.valueobject.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

@DisplayName("User")
class UserTest {

    @Nested
    @DisplayName("When creating a user")
    class  WhenCreatingAUser {

        @Test
        @DisplayName("Should create a user with PENDING status and PENDING email verification status")
        void shouldCreateUserWithPendingStatusAndPendingEmailVerificationStatus() {
            // Given
            var id = UserId.of("123e4567-e89b-12d3-a456-426614174000");
            var email = Email.of("user@example.com");
            var password = Password.of("password");

            // When
            var user = User.create(id, email, password);

            // Then
            assertThat(user.status()).isEqualTo(UserStatus.PENDING);
            assertThat(user.emailVerificationStatus()).isEqualTo(EmailVerificationStatus.PENDING);
        }

        @Test
        @DisplayName("Should create a user with the given id, email and password")
        void shouldCreateUserWithGivenIdEmailAndPassword() {
            // Given
            var id = UserId.of("123e4567-e89b-12d3-a456-426614174000");
            var email = Email.of("user@example.com");
            var password = Password.of("password");

            // When
            var user = User.create(id, email, password);

            // Then
            assertThat(user.id()).isEqualTo(id);
            assertThat(user.email()).isEqualTo(email);
            assertThat(user.password()).isEqualTo(password);
        }
    }

    @Nested
    @DisplayName("When reconstitute a user")
    class WhenReconstituteAUser {

        @Test
        @DisplayName("Should reconstitute a user with the given parameters")
        void shouldReconstituteUserWithGivenParameters() {
            // Given
            var id = UserId.of("123e4567-e89b-12d3-a456-426614174000");
            var email = Email.of("user@example.com");
            var password = Password.of("password");

            // When
            var user = User.reconstitute(id, email, password, UserStatus.PENDING, EmailVerificationStatus.PENDING, Instant.now(), Instant.now(), Instant.now());

            // Then
            assertThat(user.id()).isEqualTo(id);
            assertThat(user.email()).isEqualTo(email);
            assertThat(user.password()).isEqualTo(password);
            assertThat(user.status()).isEqualTo(UserStatus.PENDING);
            assertThat(user.emailVerificationStatus()).isEqualTo(EmailVerificationStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("When verify email")
    class WhenVerifyEmail {

        @Test
        @DisplayName("Should verify email is verified and user status is ACTIVE")
        void shouldVerifyEmail() {
            // Given
            var id = UserId.of("123e4567-e89b-12d3-a456-426614174000");
            var email = Email.of("user@example.com");
            var password = Password.of("password");

            // When
            var user = User.create(id, email, password);
            user.verifyEmail();

            // Then
            assertThat(user.emailVerificationStatus()).isEqualTo(EmailVerificationStatus.VERIFIED);
            assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should throw EmailAlreadyVerifiedException when email is already verified")
        void shouldThrowEmailAlreadyVerifiedExceptionWhenEmailIsAlreadyVerified() {
            // Given
            var id = UserId.of("123e4567-e89b-12d3-a456-426614174000");
            var email = Email.of("user@example.com");
            var password = Password.of("password");

            // When
            var user = User.create(id, email, password);
            user.verifyEmail();

            // Then
            assertThatThrownBy(user::verifyEmail)
                .isInstanceOf(EmailAlreadyVerifiedException.class).hasMessage("Email já foi verificado");
        }
    }
}