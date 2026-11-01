package dev.brunofelix.shared.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UuidV7GeneratorTest {

  @Test
  void shouldGenerateUniqueUuidVersionSeven() {
    UuidV7Generator generator = new UuidV7Generator();

    UUID first = generator.generate();
    UUID second = generator.generate();

    assertThat(first.version()).isEqualTo(7);
    assertThat(first.variant()).isEqualTo(2);
    assertThat(second).isNotEqualTo(first);
  }
}
