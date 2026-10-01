package dev.brunofelix.identity.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserTimestamps")
public class UserTimestampsTest {

  @Nested
  @DisplayName("When creating a UserTimestamps")
  class WhenCreatingUserTimestamps {

    @Test
    @DisplayName("Should create a UserTimestamps with the given start time")
    void shouldCreateUserTimestampsWithGivenStartTime() {
      // Given
      var startTime = Instant.now();

      // When
      var userTimestamps = UserTimestamps.startingAt(startTime);

      // Then
      assertThat(userTimestamps.createdAt()).isEqualTo(startTime);
      assertThat(userTimestamps.updatedAt()).isEqualTo(startTime);
    }
  }
}
