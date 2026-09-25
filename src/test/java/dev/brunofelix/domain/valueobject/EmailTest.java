package dev.brunofelix.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


@DisplayName("Email")
class EmailTest {

    @Nested
    @DisplayName("When creating from value")
    class WhenCreatingFromValue {

        @Test
        @DisplayName("should create email")
        void shouldCreateEmail() {
            // Given
            String value = "user@example.com";

            // When
            Email email = new Email(value);

            // Then
            assertThat(email.value()).isEqualTo(value);
        }

        @Test
        @DisplayName("should create email from factory method")
        void shouldCreateEmailFromFactoryMethod() {
            // Given
            String value = "user@example.com";

            // When
            Email email = Email.of(value);

            // Then
            assertThat(email.value()).isEqualTo(value);
        }

        @ParameterizedTest(name = "[{index}] should normalize ''{0}'' to ''{1}''")
        @CsvSource({"  user@example.com  , user@example.com",
                    "User@EXAMPLE.COM, user@example.com",
                    "  User@EXAMPLE.COM  , user@example.com"})
        void shouldNormalizeEmail(final String value, final String expected) {
            // When
            Email email = new Email(value);

            // Then
            assertThat(email.value()).isEqualTo(expected);
        }
        
        @Test
        @DisplayName("should reject null value")
        void shouldRejectNullValue() {
            // When & Then
            assertThatNullPointerException().isThrownBy(() -> new Email(null))
                    .withMessage("O email não pode ser nulo");
        }

        @Test
        @DisplayName("should reject empty value")
        void shouldRejectEmptyValue() {
            // When & Then
            assertThatIllegalArgumentException().isThrownBy(() -> new Email("   "))
                    .withMessage("O email não pode ser vazio");
        }

        @Test
        @DisplayName("should reject invalid email")
        void shouldRejectInvalidEmail() {
            // Given
            String invalidEmail = "invalid-email";

            // When & Then
            assertThatIllegalArgumentException().isThrownBy(() -> new Email(invalidEmail))
                    .withMessage("O email é inválido");
        }

    }

     @Nested
    @DisplayName("When comparing emails")
    class WhenComparingEmails {

        @Test
        @DisplayName("should be equal when values are equal")
        void shouldBeEqualWhenValuesAreEqual() {
            // Given
            Email first = new Email("user@example.com");
            Email second = new Email("user@example.com");

            // Then
            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        }

        @Test
        @DisplayName("should be equal when values differ only by case")
        void shouldBeEqualWhenValuesDifferOnlyByCase() {
            // Given
            Email first = new Email("User@Example.com");
            Email second = new Email("user@example.com");

            // Then
            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        }

        @Test
        @DisplayName("should not be equal when values are different")
        void shouldNotBeEqualWhenValuesAreDifferent() {
            // Given
            Email first = new Email("first@example.com");
            Email second = new Email("second@example.com");

            // Then
            assertThat(first).isNotEqualTo(second);
        }
    }

}