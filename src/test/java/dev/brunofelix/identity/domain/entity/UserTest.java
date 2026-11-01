package dev.brunofelix.identity.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.brunofelix.identity.domain.enums.EmailVerificationStatus;
import dev.brunofelix.identity.domain.enums.UserStatus;
import dev.brunofelix.identity.domain.exception.EmailAlreadyVerifiedException;
import dev.brunofelix.identity.domain.exception.EmailNotVerifiedException;
import dev.brunofelix.identity.domain.exception.UserBlockedException;
import dev.brunofelix.identity.domain.valueobject.Email;
import dev.brunofelix.identity.domain.valueobject.PasswordHash;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserTest {

  private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final Email EMAIL = Email.of("user@example.com");
  private static final PasswordHash PASSWORD_HASH = PasswordHash.of("hashed-password");

  @Test
  void shouldRegisterPendingUser() {
    User user = User.register(USER_ID, EMAIL, PASSWORD_HASH);

    assertThat(user.id()).isEqualTo(USER_ID);
    assertThat(user.email()).isEqualTo(EMAIL);
    assertThat(user.passwordHash()).isEqualTo(PASSWORD_HASH);
    assertThat(user.status()).isEqualTo(UserStatus.PENDING);
    assertThat(user.emailVerificationStatus()).isEqualTo(EmailVerificationStatus.PENDING);
    assertThat(user.lastLoginAt()).isNull();
  }

  @Test
  void shouldRestorePersistedState() {
    Instant lastLoginAt = Instant.parse("2026-01-01T12:00:00Z");

    User user =
        User.restore(
            USER_ID,
            EMAIL,
            PASSWORD_HASH,
            UserStatus.BLOCKED,
            EmailVerificationStatus.VERIFIED,
            lastLoginAt);

    assertThat(user.status()).isEqualTo(UserStatus.BLOCKED);
    assertThat(user.emailVerificationStatus()).isEqualTo(EmailVerificationStatus.VERIFIED);
    assertThat(user.lastLoginAt()).isEqualTo(lastLoginAt);
  }

  @Test
  void shouldVerifyEmailAndActivateUser() {
    User user = pendingUser();

    user.verifyEmail();

    assertThat(user.emailVerificationStatus()).isEqualTo(EmailVerificationStatus.VERIFIED);
    assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
  }

  @Test
  void shouldRejectRepeatedEmailVerification() {
    User user = pendingUser();
    user.verifyEmail();

    assertThatThrownBy(user::verifyEmail)
        .isInstanceOf(EmailAlreadyVerifiedException.class)
        .hasMessage("Email já foi verificado");
  }

  @Test
  void shouldRejectActivationBeforeEmailVerification() {
    User user = pendingUser();

    assertThatThrownBy(user::activate).isInstanceOf(EmailNotVerifiedException.class);
  }

  @Test
  void shouldRegisterLoginAtInformedTime() {
    User user = pendingUser();
    user.verifyEmail();
    Instant occurredAt = Instant.parse("2026-01-01T12:00:00Z");

    user.registerLogin(occurredAt);

    assertThat(user.lastLoginAt()).isEqualTo(occurredAt);
  }

  @Test
  void shouldRejectLoginWhenUserIsBlocked() {
    User user = pendingUser();
    user.verifyEmail();
    user.block();

    assertThat(user.canLogin()).isFalse();
    assertThatThrownBy(() -> user.registerLogin(Instant.now()))
        .isInstanceOf(UserBlockedException.class);
  }

  @Test
  void shouldChangePasswordHash() {
    User user = pendingUser();
    PasswordHash newPasswordHash = PasswordHash.of("new-hashed-password");

    user.changePassword(newPasswordHash);

    assertThat(user.passwordHash()).isEqualTo(newPasswordHash);
  }

  private static User pendingUser() {
    return User.register(USER_ID, EMAIL, PASSWORD_HASH);
  }
}
