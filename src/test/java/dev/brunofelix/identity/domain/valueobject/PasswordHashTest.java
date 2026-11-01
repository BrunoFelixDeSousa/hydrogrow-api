package dev.brunofelix.identity.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

class PasswordHashTest {

  @Test
  void shouldNormalizeAndProtectHash() {
    PasswordHash hash = PasswordHash.of("  $argon2id$hash  ");

    assertThat(hash.value()).isEqualTo("$argon2id$hash");
    assertThat(hash).hasToString("********");
  }

  @Test
  void shouldRejectNullHash() {
    assertThatNullPointerException()
        .isThrownBy(() -> PasswordHash.of(null))
        .withMessage("O hash da senha não pode ser nulo");
  }

  @Test
  void shouldRejectBlankHash() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> PasswordHash.of("   "))
        .withMessage("O hash da senha não pode ser vazio");
  }
}
