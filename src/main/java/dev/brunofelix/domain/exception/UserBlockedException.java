package dev.brunofelix.domain.exception;

public final class UserBlockedException extends DomainException {

    public UserBlockedException() {
        super("Usuário está bloqueado")
    }
}
