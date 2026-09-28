package dev.brunofelix.identity.domain.valueobject;

import dev.brunofelix.identity.domain.enums.EmailVerificationStatus;
import dev.brunofelix.identity.domain.enums.UserStatus;

import java.util.Objects;

public record UserAccountStat (UserStatus status, EmailVerificationStatus emailVerificationStatus) {

    public UserAccountStat {
        Objects.requireNonNull(status, "O status do usuário não pode ser nulo");
        Objects.requireNonNull(emailVerificationStatus, "O status de verificação do e-mail não pode ser nulo");
    }

    public static UserAccountStat pending() {
        return new UserAccountStat(UserStatus.PENDING, EmailVerificationStatus.PENDING);
    }

}