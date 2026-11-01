package dev.brunofelix.tenancy.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class PlanTest {

  @Test
  void shouldMapDatabaseCodes() {
    assertThat(Plan.fromCode("basico")).isEqualTo(Plan.BASIC);
    assertThat(Plan.fromCode("profissional")).isEqualTo(Plan.PROFESSIONAL);
    assertThat(Plan.BASIC.code()).isEqualTo("basico");
  }

  @Test
  void shouldRejectUnknownCode() {
    assertThatIllegalArgumentException().isThrownBy(() -> Plan.fromCode("desconhecido"));
  }
}
