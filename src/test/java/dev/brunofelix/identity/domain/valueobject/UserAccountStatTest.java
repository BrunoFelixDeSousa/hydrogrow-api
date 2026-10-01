package dev.brunofelix.identity.domain.valueobject;

import static org.assertj.core.api.Assertions.assertThat;

import dev.brunofelix.identity.domain.enums.EmailVerificationStatus;
import dev.brunofelix.identity.domain.enums.UserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("UserAccountStat")
public class UserAccountStatTest {

  @Nested
  @DisplayName("When creating a UserAccountStat")
  class WhenCreatingUserAccountStat {

    @Test
    @DisplayName("Should create a UserAccountStat with given status and email verification status")
    void shouldCreateUserAccountStatWithGivenStatusAndEmailVerificationStatus() {
      // Given
      var status = UserStatus.ACTIVE;
      var emailVerificationStatus = EmailVerificationStatus.VERIFIED;

      // When
      var userAccountStat = new UserAccountState(status, emailVerificationStatus);

      // Then
      assertThat(userAccountStat.status()).isEqualTo(status);
      assertThat(userAccountStat.emailVerificationStatus()).isEqualTo(emailVerificationStatus);
    }

    @Test
    @DisplayName(
        "Should create a UserAccountStat with PENDING status and PENDING email verification status")
    void shouldCreateUserAccountStatWithPendingStatusAndPendingEmailVerificationStatus() {
      // Given
      var userAccountStat = UserAccountState.pending();

      // When & Then
      assertThat(userAccountStat.status()).isEqualTo(UserStatus.PENDING);
      assertThat(userAccountStat.emailVerificationStatus())
          .isEqualTo(EmailVerificationStatus.PENDING);
    }
  }
}
