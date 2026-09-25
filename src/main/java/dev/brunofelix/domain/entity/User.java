package dev.brunofelix.domain.entity;

import dev.brunofelix.domain.enums.EmailVerificationStatus;
import dev.brunofelix.domain.enums.UserStatus;
import dev.brunofelix.domain.exception.EmailAlreadyVerifiedException;
import dev.brunofelix.domain.exception.EmailNotVerifiedException;
import dev.brunofelix.domain.exception.UserAlreadyActiveException;
import dev.brunofelix.domain.exception.UserBlockedException;
import dev.brunofelix.domain.exception.UserInactiveException;
import dev.brunofelix.domain.valueobject.Email;
import dev.brunofelix.domain.valueobject.Password;
import dev.brunofelix.domain.valueobject.UserId;

import java.time.Instant;
import java.util.Objects;

public class User {

    private final UserId id;
    private final Email email;
    private Password password;
    private UserStatus status;
    private EmailVerificationStatus emailVerificationStatus;
    private Instant lastLoginAt;
    private final Instant createdAt;
    private Instant updatedAt;

    private User(
            final UserId id,
            final Email email,
            final Password password,
            final UserStatus status,
            final EmailVerificationStatus emailVerificationStatus,
            final Instant createdAt,
            final Instant updatedAt
    ) {
        this.id = Objects.requireNonNull(id, "O identificador não pode ser nulo");
        this.email = Objects.requireNonNull(email, "O e-mail não pode ser nulo");
        this.password = Objects.requireNonNull(password, "A senha não pode ser nula");
        this.status = Objects.requireNonNull(status, "O status do usuário não pode ser nulo");
        this.emailVerificationStatus = Objects.requireNonNull(emailVerificationStatus, "O status de verificação do e-mail não pode ser nulo");
        this.lastLoginAt = lastLoginAt;
        this.createdAt = Objects.requireNonNull(createdAt, "A data de criação não pode ser nula");
        this.updatedAt = Objects.requireNonNull(updatedAt, "A data de atualização não pode ser nula");
    }

    public static User create(
            final UserId id, 
            final Email email, 
            final Password password
    ) {
        Instant now = Instant.now();

        return new User(
            id,
            email,
            password,
            UserStatus.PENDING,
            EmailVerificationStatus.PENDING,
            null,
            now,
            now
        );
    }

    public static User reconstitute(
            final UserId id, 
            final Email email, 
            final Password password,
            final UserStatus status, 
            final EmailVerificationStatus verificationStatus,
            final Instant lastLoginAt, 
            final Instant createdAt, 
            final Instant updatedAt 
    ) {
        return new User(
            id,
            email,
            password,
            UserStatus.PENDING,
            EmailVerificationStatus.PENDING,
            lastLoginAt,
            now,
            now
        );
    }

    public void verifyEmail() {
        if (emailVerificationStatus.isVerified()) {
            throw new EmailAlreadyVerifiedException();
        }

        emailVerificationStatus = EmailVerificationStatus.ACTIVE;
        status = UserStatus.ACTIVE;

        touch();
    }

    public void activate() {
        if (status == UserStatus.ACTIVE) {
            throw new UserAlreadyActiveException();
        }

        if (!emailVerificationStatus.isVerified) {
            throw new EmailNotVerifiedException();
        }

        status = UserStatus.ACTIVE;

        touch();
    }

    public void block() {
        if (status == UserStatus.BLOCKED) {
            return;
        }

        status = UserStatus.BLOCKED;

        touch();
    }

    public void disable() {
        if (status == UserStatus.DISABLED) {
            return;
        }

        status = UserStatus.DISABLED;

        touch();
    }

    public void changePassword(final Password password) {
        this.password = Objects.requireNonNull(password, "A senha não pode ser nula");

        touch();
    }

    public void registerLogin() {
        ensureCanLogin();

        lastLoginAt = Instant.now();

        touch();
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

    private void touch() {
        updatedAt = Instant.now();
    }

    public UserId id() {
        return id;
    }

    public Email email() {
        return email;
    }

    public Password password() {
        return password;
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

    public Instant createdAt() {
        return createdAt;
    }

    public Instant updatedAt() {
        return updatedAt;
    }
}