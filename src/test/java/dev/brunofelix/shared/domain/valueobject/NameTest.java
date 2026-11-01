package dev.brunofelix.shared.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import org.junit.jupiter.api.Test;

class NameTest {

  @Test
  void shouldNormalizeName() {
    assertThat(Name.of("  Hydro Grow  ").value()).isEqualTo("Hydro Grow");
  }

  @Test
  void shouldRejectNullName() {
    assertThatNullPointerException()
        .isThrownBy(() -> Name.of(null))
        .withMessage("O nome não pode ser nulo");
  }

  @Test
  void shouldRejectNameOutsideAllowedLength() {
    assertThatIllegalArgumentException().isThrownBy(() -> Name.of("A"));
    assertThatIllegalArgumentException().isThrownBy(() -> Name.of("A".repeat(101)));
  }
}
