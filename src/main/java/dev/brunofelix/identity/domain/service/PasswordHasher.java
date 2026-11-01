package dev.brunofelix.identity.domain.service;

import dev.brunofelix.identity.domain.valueobject.PasswordHash;

public interface PasswordHasher {

  PasswordHash hash(final String password);

  boolean matches(final String password, final PasswordHash passwordHash);
}
