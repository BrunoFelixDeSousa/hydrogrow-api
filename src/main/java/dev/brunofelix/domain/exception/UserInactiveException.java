package dev.brunofelix.domain.exception;

public final class UserInactiveException extends DomainException {

    public UserInactiveException() {
        super("Usuário está inativo");
    }
}
