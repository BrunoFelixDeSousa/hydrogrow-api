package dev.brunofelix.identity.application.port;

import dev.brunofelix.identity.domain.entity.User;
import dev.brunofelix.identity.domain.valueobject.Email;

public interface UserStore {

  boolean emailExists(final Email email);

  void insert(final User user);
}
