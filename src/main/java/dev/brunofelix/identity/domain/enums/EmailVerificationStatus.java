package dev.brunofelix.identity.domain.enums;

public enum EmailVerificationStatus {
  PENDING,
  VERIFIED,
  FAILED;

  public boolean isVerified() {
    return this == VERIFIED;
  }
}
