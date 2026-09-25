package dev.brunofelix.domain.exception;

public final class EmailAlreadyVerifiedException extends DomainException {

    public EmailAlreadyVerifiedException() {
        super("Email já foi verificado");
    }
}