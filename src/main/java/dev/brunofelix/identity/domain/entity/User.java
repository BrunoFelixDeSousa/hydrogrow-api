package dev.brunofelix.identity.domain.entity;

import dev.brunofelix.identity.domain.enums.EmailVerificationStatus;
import dev.brunofelix.identity.domain.enums.UserStatus;
import dev.brunofelix.identity.domain.exception.EmailAlreadyVerifiedException;
import dev.brunofelix.identity.domain.exception.EmailNotVerifiedException;
import dev.brunofelix.identity.domain.exception.UserAlreadyActiveException;
import dev.brunofelix.identity.domain.exception.UserBlockedException;
import dev.brunofelix.identity.domain.exception.UserInactiveException;
import dev.brunofelix.identity.domain.valueobject.Email;
import dev.brunofelix.identity.domain.valueobject.PasswordHash;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public final class User {

  private final UUID id;
  private final Email email;
  private PasswordHash passwordHash;
  private UserStatus status;
  private EmailVerificationStatus emailVerificationStatus;
  private Instant lastLoginAt;

  private User(
      final UUID id,
      final Email email,
      final PasswordHash passwordHash,
      final UserStatus status,
      final EmailVerificationStatus emailVerificationStatus,
      final Instant lastLoginAt) {
    this.id = Objects.requireNonNull(id, "O identificador não pode ser nulo");
    this.email = Objects.requireNonNull(email, "O e-mail não pode ser nulo");
    this.passwordHash = Objects.requireNonNull(passwordHash, "O hash da senha não pode ser nulo");
    this.status = Objects.requireNonNull(status, "O status da conta não pode ser nulo");
    this.emailVerificationStatus =
        Objects.requireNonNull(
            emailVerificationStatus, "O estado de verificação do e-mail não pode ser nulo");
    this.lastLoginAt = lastLoginAt;
  }

  public static User register(final UUID id, final Email email, final PasswordHash passwordHash) {
    return new User(
        id, email, passwordHash, UserStatus.PENDING, EmailVerificationStatus.PENDING, null);
  }

  public static User restore(
      final UUID id,
      final Email email,
      final PasswordHash passwordHash,
      final UserStatus status,
      final EmailVerificationStatus emailVerificationStatus,
      final Instant lastLoginAt) {
    return new User(id, email, passwordHash, status, emailVerificationStatus, lastLoginAt);
  }

  public void verifyEmail() {
    if (emailVerificationStatus.isVerified()) {
      throw new EmailAlreadyVerifiedException();
    }

    emailVerificationStatus = EmailVerificationStatus.VERIFIED;
    status = UserStatus.ACTIVE;
  }

  public void activate() {
    if (status == UserStatus.ACTIVE) {
      throw new UserAlreadyActiveException();
    }

    if (!emailVerificationStatus.isVerified()) {
      throw new EmailNotVerifiedException();
    }

    status = UserStatus.ACTIVE;
  }

  public void block() {
    if (status == UserStatus.BLOCKED) {
      return;
    }

    status = UserStatus.BLOCKED;
  }

  public void disable() {
    if (status == UserStatus.DISABLED) {
      return;
    }

    status = UserStatus.DISABLED;
  }

  public void changePassword(final PasswordHash passwordHash) {
    this.passwordHash = Objects.requireNonNull(passwordHash, "O hash da senha não pode ser nulo");
  }

  public void registerLogin(final Instant occurredAt) {
    ensureCanLogin();
    lastLoginAt = Objects.requireNonNull(occurredAt, "A data do login não pode ser nula");
  }

  public boolean canLogin() {
    return status.canAuthenticate() && emailVerificationStatus.isVerified();
  }

  private void ensureCanLogin() {
    if (status.isBlocked()) {
      throw new UserBlockedException();
    }

    if (!status.canAuthenticate()) {
      throw new UserInactiveException();
    }

    if (!emailVerificationStatus.isVerified()) {
      throw new EmailNotVerifiedException();
    }
  }

  public UUID id() {
    return id;
  }

  public Email email() {
    return email;
  }

  public PasswordHash passwordHash() {
    return passwordHash;
  }

  public UserStatus status() {
    return status;
  }

  public EmailVerificationStatus emailVerificationStatus() {
    return emailVerificationStatus;
  }

  public Instant lastLoginAt() {
    return lastLoginAt;
  }
}
