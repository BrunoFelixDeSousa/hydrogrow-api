package dev.brunofelix.identity.domain.entity;

import dev.brunofelix.identity.domain.valueobject.ProfileId;
import dev.brunofelix.identity.domain.valueobject.UserId;
import dev.brunofelix.shared.domain.EntityValidation;
import dev.brunofelix.shared.domain.valueobject.Name;
import java.time.Instant;

public record Profile(
    ProfileId id,
    UserId userId,
    Name fullName,
    String avatarUrl,
    Instant createdAt,
    Instant updatedAt) {
  public Profile {
    id = EntityValidation.required(id, "O identificador do perfil é obrigatório");
    userId = EntityValidation.required(userId, "O identificador do usuário é obrigatório");
    fullName = EntityValidation.required(fullName, "O nome completo é obrigatório");
    createdAt = EntityValidation.required(createdAt, "A data de criação do perfil é obrigatória");
    updatedAt =
        EntityValidation.required(updatedAt, "A data de atualização do perfil é obrigatória");
  }

  public static Profile create(
      final ProfileId id, final UserId userId, final Name fullName, final Instant createdAt) {
    return new Profile(id, userId, fullName, null, createdAt, createdAt);
  }
}
