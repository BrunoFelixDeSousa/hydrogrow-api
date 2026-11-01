package dev.brunofelix.identity.domain.entity;

import static org.assertj.core.api.Assertions.assertThat;

import dev.brunofelix.shared.domain.valueobject.Name;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProfileTest {

  @Test
  void shouldUseUserAsIdentityAndUpdateProfileData() {
    UUID userId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    Profile profile = Profile.create(userId, Name.of("Bruno Felix"));

    profile.rename(Name.of("Bruno F. Felix"));
    profile.changeAvatar("  https://example.com/avatar.png  ");

    assertThat(profile.userId()).isEqualTo(userId);
    assertThat(profile.fullName()).isEqualTo(Name.of("Bruno F. Felix"));
    assertThat(profile.avatarUrl()).isEqualTo("https://example.com/avatar.png");
  }
}
