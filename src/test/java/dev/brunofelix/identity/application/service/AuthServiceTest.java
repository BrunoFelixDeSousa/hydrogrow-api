package dev.brunofelix.identity.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AuthServiceTest {

  private static final UUID USER_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
  private static final UUID TENANT_ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174001");
  private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
  private static final Email EMAIL = Email.of("user@example.com");
  private static final PasswordHash PASSWORD_HASH = PasswordHash.of("hashed-current-password");

  private SequenceIdGenerator ids;
  private InMemoryUserStore users;
  private CapturingTenantStore tenants;
  private CapturingProfileStore profiles;
  private CapturingMembershipStore memberships;
  private AuthService service;

  @BeforeEach
  void setUp() {
    ids = new SequenceIdGenerator(USER_ID, TENANT_ID);
    users = new InMemoryUserStore();
    tenants = new CapturingTenantStore();
    profiles = new CapturingProfileStore();
    memberships = new CapturingMembershipStore();
    service =
        new AuthService(
            ids,
            users,
            new StubPasswordHasher(),
            tenants,
            memberships,
            profiles,
            Clock.fixed(NOW, ZoneOffset.UTC));
  }

  @Test
  void shouldCreateSignupAggregateGraphWithOnlyRootIds() {
    service.signup("User@Example.com", "plain-password", "Bruno Felix", "Hydro Grow");

    User user = users.findById(USER_ID).orElseThrow();
    assertThat(ids.isExhausted()).isTrue();
    assertThat(user.id()).isEqualTo(USER_ID);
    assertThat(user.status()).isEqualTo(UserStatus.PENDING);
    assertThat(user.emailVerificationStatus()).isEqualTo(EmailVerificationStatus.PENDING);
    assertThat(tenants.inserted.id()).isEqualTo(TENANT_ID);
    assertThat(tenants.inserted.plan()).isEqualTo(Plan.BASIC);
    assertThat(profiles.inserted.userId()).isEqualTo(USER_ID);
    assertThat(memberships.inserted.tenantId()).isEqualTo(TENANT_ID);
    assertThat(memberships.inserted.userId()).isEqualTo(USER_ID);
  }

  @Test
  void shouldRejectSignupWhenEmailIsAlreadyInUse() {
    users.insert(pendingUser());

    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> service.signup("USER@example.com", "plain-password", "Bruno Felix", "Hydro Grow"))
        .withMessage("E-mail já está em uso");

    assertThat(tenants.inserted).isNull();
    assertThat(profiles.inserted).isNull();
    assertThat(memberships.inserted).isNull();
  }

  @Test
  void shouldVerifyEmailAndPersistUser() {
    User user = pendingUser();
    users.insert(user);

    service.verifyEmail(USER_ID);

    assertThat(user.emailVerificationStatus()).isEqualTo(EmailVerificationStatus.VERIFIED);
    assertThat(user.status()).isEqualTo(UserStatus.ACTIVE);
    assertThat(users.updated).isSameAs(user);
  }

  @Test
  void shouldAuthenticateActiveUserAndRegisterLoginTime() {
    User user = activeUser();
    users.insert(user);

    UUID authenticatedUserId = service.authenticate(" USER@example.com ", "current-password");

    assertThat(authenticatedUserId).isEqualTo(USER_ID);
    assertThat(user.lastLoginAt()).isEqualTo(NOW);
    assertThat(users.updated).isSameAs(user);
  }

  @Test
  void shouldNotRevealWhetherEmailOrPasswordIsInvalid() {
    users.insert(activeUser());

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.authenticate("user@example.com", "wrong-password"))
        .withMessage("E-mail ou senha inválidos");
    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.authenticate("unknown@example.com", "current-password"))
        .withMessage("E-mail ou senha inválidos");

    assertThat(users.updated).isNull();
  }

  @Test
  void shouldChangePasswordAfterCheckingCurrentPassword() {
    User user = activeUser();
    users.insert(user);

    service.changePassword(USER_ID, "current-password", "new-password");

    assertThat(user.passwordHash()).isEqualTo(PasswordHash.of("hashed-new-password"));
    assertThat(users.updated).isSameAs(user);
  }

  @Test
  void shouldRejectPasswordChangeWhenCurrentPasswordIsInvalid() {
    User user = activeUser();
    users.insert(user);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.changePassword(USER_ID, "wrong-password", "new-password"))
        .withMessage("E-mail ou senha inválidos");

    assertThat(user.passwordHash()).isEqualTo(PASSWORD_HASH);
    assertThat(users.updated).isNull();
  }

  @Test
  void shouldRejectOperationsForUnknownUser() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.verifyEmail(USER_ID))
        .withMessage("Usuário não encontrado");
    assertThatIllegalArgumentException()
        .isThrownBy(() -> service.changePassword(USER_ID, "current-password", "new-password"))
        .withMessage("Usuário não encontrado");
  }

  private static User pendingUser() {
    return User.register(USER_ID, EMAIL, PASSWORD_HASH);
  }

  private static User activeUser() {
    User user = pendingUser();
    user.verifyEmail();
    return user;
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

  private static final class InMemoryUserStore implements UserStore {

    private final Map<UUID, User> users = new HashMap<>();
    private User updated;

    @Override
    public Optional<User> findById(final UUID id) {
      return Optional.ofNullable(users.get(id));
    }

    @Override
    public Optional<User> findByEmail(final Email email) {
      return users.values().stream().filter(user -> user.email().equals(email)).findFirst();
    }

    @Override
    public void insert(final User user) {
      users.put(user.id(), user);
    }

    @Override
    public void update(final User user) {
      users.put(user.id(), user);
      updated = user;
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
