package dev.brunofelix.domain.valueobject;

import java.util.Objects;

public record Password(String hashedValue) {

    public Password {
        Objects.requireNonNull(hashedValue, "A senha não pode ser nula");

        hashedValue = hashedValue.trim();
        
        if (hashedValue.isBlank()) {
            throw new IllegalArgumentException("A senha não pode ser vazia");
        }
    }

    public static Password of(final String hashedValue) {
        return new Password(hashedValue);
    }

    @Override
    public String toString() {
        return "********";
    }
}