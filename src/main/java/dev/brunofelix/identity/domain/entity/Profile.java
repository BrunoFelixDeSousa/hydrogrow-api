package dev.brunofelix.identity.domain.entity;

import dev.brunofelix.shared.domain.valueobject.Name;
import java.util.Objects;
import java.util.UUID;

public final class Profile {

  private final UUID userId;
  private Name fullName;
  private String avatarUrl;

  private Profile(final UUID userId, final Name fullName, final String avatarUrl) {
    this.userId = Objects.requireNonNull(userId, "O identificador do usuário é obrigatório");
    this.fullName = Objects.requireNonNull(fullName, "O nome completo é obrigatório");
    this.avatarUrl = normalizeOptional(avatarUrl);
  }

  public static Profile create(final UUID userId, final Name fullName) {
    return new Profile(userId, fullName, null);
  }

  public static Profile restore(final UUID userId, final Name fullName, final String avatarUrl) {
    return new Profile(userId, fullName, avatarUrl);
  }

  public void rename(final Name fullName) {
    this.fullName = Objects.requireNonNull(fullName, "O nome completo é obrigatório");
  }

  public void changeAvatar(final String avatarUrl) {
    this.avatarUrl = normalizeOptional(avatarUrl);
  }

  private static String normalizeOptional(final String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  public UUID userId() {
    return userId;
  }

  public Name fullName() {
    return fullName;
  }

  public String avatarUrl() {
    return avatarUrl;
  }
}
