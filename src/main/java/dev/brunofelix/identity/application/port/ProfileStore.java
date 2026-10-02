package dev.brunofelix.identity.application.port;

import dev.brunofelix.identity.domain.entity.Profile;
import dev.brunofelix.identity.domain.valueobject.ProfileId;
import java.util.Optional;

public interface ProfileStore {

  Optional<Profile> find(final ProfileId id);

  void insert(final Profile profile);
}
