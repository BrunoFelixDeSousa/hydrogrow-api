package dev.brunofelix.domain.valueobject;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
            Pattern.CASE_INSENSITIVE
    );

    public Email {
        Objects.requireNonNull(value, "O email não pode ser nulo");

        value = value.trim().toLowerCase(Locale.ROOT);

        if (value.isBlank()) {
            throw new IllegalArgumentException("O email não pode ser vazio");
        }

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("O email é inválido");
        }
    }

    public static Email of(final String value) {
        return new Email(value);
    }

    @Override
    public String toString() {
        return value;
    }
}