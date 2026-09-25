package dev.brunofelix.domain.exception;

public abstract class DomainException extends RuntimeException {

    protected DomainException(final String message) {
        super(message);
    }
}