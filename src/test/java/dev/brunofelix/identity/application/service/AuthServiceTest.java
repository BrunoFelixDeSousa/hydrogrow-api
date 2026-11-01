package dev.brunofelix.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import dev.brunofelix.identity.application.port.ProfileStore;
import dev.brunofelix.identity.application.port.UserStore;
import dev.brunofelix.identity.domain.entity.Profile;
import dev.brunofelix.identity.domain.entity.User;
import dev.brunofelix.identity.domain.enums.EmailVerificationStatus;
import dev.brunofelix.identity.domain.enums.UserStatus;
import dev.brunofelix.identity.domain.service.PasswordHasher;
import dev.brunofelix.identity.domain.valueobject.Email;
import dev.brunofelix.identity.domain.valueobject.PasswordHash;
import dev.brunofelix.shared.service.IdGenerator;
import dev.brunofelix.tenancy.application.port.MembershipStore;
import dev.brunofelix.tenancy.application.port.TenantStore;
import dev.brunofelix.tenancy.domain.entity.Membership;
import dev.brunofelix.tenancy.domain.entity.Tenant;
import dev.brunofelix.tenancy.domain.enums.Plan;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

  private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");

  @Test
  void shouldCreateSignupAggregateGraphWithOnlyRootIds() {
    SequenceIdGenerator ids = new SequenceIdGenerator(USER_ID, TENANT_ID);
    CapturingUserStore users = new CapturingUserStore();
    CapturingTenantStore tenants = new CapturingTenantStore();
    CapturingProfileStore profiles = new CapturingProfileStore();
    CapturingMembershipStore memberships = new CapturingMembershipStore();
    AuthService service =
        new AuthService(ids, users, new StubPasswordHasher(), tenants, memberships, profiles);

    service.signup("User@Example.com", "plain-password", "Bruno Felix", "Hydro Grow");

    assertThat(ids.isExhausted()).isTrue();
    assertThat(users.inserted.id()).isEqualTo(USER_ID);
    assertThat(users.inserted.status()).isEqualTo(UserStatus.PENDING);
    assertThat(users.inserted.emailVerificationStatus()).isEqualTo(EmailVerificationStatus.PENDING);
    assertThat(tenants.inserted.id()).isEqualTo(TENANT_ID);
    assertThat(tenants.inserted.plan()).isEqualTo(Plan.BASIC);
    assertThat(profiles.inserted.userId()).isEqualTo(USER_ID);
    assertThat(memberships.inserted.tenantId()).isEqualTo(TENANT_ID);
    assertThat(memberships.inserted.userId()).isEqualTo(USER_ID);
  }

  private static final class SequenceIdGenerator implements IdGenerator {

    private final Queue<UUID> ids;

    private SequenceIdGenerator(final UUID... ids) {
      this.ids = new ArrayDeque<>(List.of(ids));
    }

    @Override
    public UUID generate() {
      return ids.remove();
    }

    private boolean isExhausted() {
      return ids.isEmpty();
    }
  }

  private static final class StubPasswordHasher implements PasswordHasher {

    @Override
    public PasswordHash hash(final String password) {
      return PasswordHash.of("hashed-" + password);
    }

    @Override
    public boolean matches(final String password, final PasswordHash passwordHash) {
      return passwordHash.equals(hash(password));
    }
  }

  private static final class CapturingUserStore implements UserStore {

    private User inserted;

    @Override
    public boolean emailExists(final Email email) {
      return false;
    }

    @Override
    public void insert(final User user) {
      inserted = user;
    }
  }

  private static final class CapturingTenantStore implements TenantStore {

    private Tenant inserted;

    @Override
    public Optional<Tenant> find(final UUID id) {
      return Optional.empty();
    }

    @Override
    public void insert(final Tenant tenant) {
      inserted = tenant;
    }
  }

  private static final class CapturingProfileStore implements ProfileStore {

    private Profile inserted;

    @Override
    public Optional<Profile> findByUserId(final UUID userId) {
      return Optional.empty();
    }

    @Override
    public void insert(final Profile profile) {
      inserted = profile;
    }
  }

  private static final class CapturingMembershipStore implements MembershipStore {

    private Membership inserted;

    @Override
    public boolean exists(final UUID tenantId, final UUID userId) {
      return false;
    }

    @Override
    public void insert(final Membership membership) {
      inserted = membership;
    }
  }
}
