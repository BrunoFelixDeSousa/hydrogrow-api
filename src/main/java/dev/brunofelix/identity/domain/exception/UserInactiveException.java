package dev.brunofelix.identity.domain.exception;

public final class UserInactiveException extends DomainException {

    public UserInactiveException() {
        super("Usuário está inativo");
    }
}
