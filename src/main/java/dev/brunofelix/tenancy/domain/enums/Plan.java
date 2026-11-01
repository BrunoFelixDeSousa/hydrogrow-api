package dev.brunofelix.tenancy.domain.enums;

import java.util.Arrays;

public enum Plan {
  BASIC("basico"),
  PROFESSIONAL("profissional");

  private final String code;

  Plan(final String code) {
    this.code = code;
  }

  public String code() {
    return code;
  }

  public static Plan fromCode(final String code) {
    return Arrays.stream(values())
        .filter(plan -> plan.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Plano desconhecido: " + code));
  }
}
