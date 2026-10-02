package dev.brunofelix.identity.domain.service;

import dev.brunofelix.identity.domain.valueobject.Password;

public interface PasswordHasher {

  Password hash(final String password);

  boolean matches(final String password, final Password hashedPassword);
}
