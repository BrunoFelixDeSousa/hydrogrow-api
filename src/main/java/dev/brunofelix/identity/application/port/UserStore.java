package dev.brunofelix.identity.application.port;

import dev.brunofelix.identity.domain.entity.User;
import dev.brunofelix.identity.domain.valueobject.Email;
import java.util.Optional;
import java.util.UUID;

public interface UserStore {

  Optional<User> findById(final UUID id);

  Optional<User> findByEmail(final Email email);

  void insert(final User user);

  void update(final User user);
}
