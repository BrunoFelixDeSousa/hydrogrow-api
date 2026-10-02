package dev.brunofelix.tenancy.domain.enums;

public enum Plan {
  BASIC,
  PRO,
  PREMIUM;

  public boolean isPremium() {
    return this == PREMIUM;
  }

  public boolean isPro() {
    return this == PRO;
  }

  public boolean isBasic() {
    return this == BASIC;
  }
}
