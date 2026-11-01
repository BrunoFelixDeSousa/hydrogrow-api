package dev.brunofelix.identity.application.port;

import dev.brunofelix.identity.domain.entity.Profile;
import java.util.Optional;
import java.util.UUID;

public interface ProfileStore {

  Optional<Profile> findByUserId(final UUID userId);

  void insert(final Profile profile);
}
