package dev.brunofelix.domain.exception;

public final class UserAlreadyActiveException extends DomainException {

    public UserAlreadyActiveException() {
        super("Usuário já está ativo")
    }
}
