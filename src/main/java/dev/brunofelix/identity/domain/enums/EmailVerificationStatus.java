package dev.brunofelix.identity.domain.enums;

public enum EmailVerificationStatus {

    PENDING,
    VERIFIED;

    public boolean isVerified() {
        return this == VERIFIED;
    }
}