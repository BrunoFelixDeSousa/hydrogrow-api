package dev.brunofelix.domain.enums;

public enum UserStatus {

    PENDING,
    ACTIVE,
    BLOCKED,
    DISABLED;

    public boolean canAuthenticate() {
        return this == ACTIVE;
    }

    public boolean isBlocked() {
        return this == BLOCKED;
    }

}