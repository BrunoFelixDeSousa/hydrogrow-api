package dev.brunofelix.identity.domain.valueobject;

import dev.brunofelix.identity.domain.enums.EmailVerificationStatus;
import dev.brunofelix.identity.domain.enums.UserStatus;

import java.util.Objects;

public record UserAccountState (UserStatus status, EmailVerificationStatus emailVerificationStatus) {

    public UserAccountState {
        Objects.requireNonNull(status, "O status do usuário não pode ser nulo");
        Objects.requireNonNull(emailVerificationStatus, "O status de verificação do e-mail não pode ser nulo");
    }

    public static UserAccountState pending() {
        return new UserAccountState(UserStatus.PENDING, EmailVerificationStatus.PENDING);
    }

}