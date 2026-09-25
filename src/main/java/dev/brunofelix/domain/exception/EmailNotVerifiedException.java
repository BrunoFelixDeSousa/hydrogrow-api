package dev.brunofelix.domain.exception;

public final class EmailNotVerifiedException extends DomainException {

    public EmailNotVerifiedException() {
        super("Email não foi verificado")
    }
}