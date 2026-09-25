package dev.brunofelix.domain.enums;

public enum EmailVerificationStatus {

    PENDING,
    VERIFIED;

    public boolean isVerified() {
        return this == VERIFIED;
    }
}