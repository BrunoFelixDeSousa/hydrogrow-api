package dev.brunofelix.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Password")
public class PasswordTest {

    @Nested
    @DisplayName("When creating from hashedValue")
    class WhenCreatingFromhashedValue {

        @Test
        @DisplayName("should create password with valid hash")
        void shouldCreatePasswordWithValidHash() {
            // Given
            String hashedValue = "$argon2id$v=19$m=65536,t=3,p=4$example";

            // When
            Password password = new Password(hashedValue);

            // Then
            assertThat(password.hashedValue()).isEqualTo(hashedValue);
        }

        @Test
        @DisplayName("should create password from factory method")
        void shouldCreatePasswordFromFactoryMethod() {
            // Given
            String hashedValue = "$argon2id$v=19$m=65536,t=3,p=4$example";

            // When
            Password password = Password.of(hashedValue);

            // Then
            assertThat(password.hashedValue()).isEqualTo(hashedValue);
        }

        @Test
        @DisplayName("should trim hashedValue")
        void shouldTrimhashedValue() {
            // Given
            String hashedValue = "  $argon2id$v=19$m=65536,t=3,p=4$example  ";

            // When
            Password password = new Password(hashedValue);

            // Then
            assertThat(password.hashedValue()).isEqualTo("$argon2id$v=19$m=65536,t=3,p=4$example");
        }

        @Test
        @DisplayName("should reject null hashedValue")
        void shouldRejectNullhashedValue() {
            // When / Then
            assertThatNullPointerException().isThrownBy(() -> new Password(null))
                    .withMessage("A senha não pode ser nula");
        }

        @Test
        @DisplayName("should reject empty hashedValue")
        void shouldRejectEmptyhashedValue() {
            // When / Then
            assertThatIllegalArgumentException().isThrownBy(() -> new Password(""))
                    .withMessage("A senha não pode ser vazia");
        }

        @Test
        @DisplayName("should reject blank hashedValue")
        void shouldRejectBlankhashedValue() {
            // When / Then
            assertThatIllegalArgumentException().isThrownBy(() -> new Password("   "))
                    .withMessage("A senha não pode ser vazia");
        }

        @Test
        @DisplayName("should reject hashedValue containing only whitespace")
        void shouldRejecthashedValueContainingOnlyWhitespace() {
            // When / Then
            assertThatIllegalArgumentException().isThrownBy(() -> new Password("\t\n"))
                    .withMessage("A senha não pode ser vazia");
        }
    }

}