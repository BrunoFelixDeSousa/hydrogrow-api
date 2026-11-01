package dev.brunofelix.identity.application.service;

import dev.brunofelix.identity.application.port.ProfileStore;
import dev.brunofelix.identity.application.port.UserStore;
import dev.brunofelix.identity.domain.entity.Profile;
import dev.brunofelix.identity.domain.entity.User;
import dev.brunofelix.identity.domain.service.PasswordHasher;
import dev.brunofelix.identity.domain.valueobject.Email;
import dev.brunofelix.shared.domain.valueobject.Name;
import dev.brunofelix.shared.service.IdGenerator;
import dev.brunofelix.tenancy.application.port.MembershipStore;
import dev.brunofelix.tenancy.application.port.TenantStore;
import dev.brunofelix.tenancy.domain.entity.Membership;
import dev.brunofelix.tenancy.domain.entity.Tenant;
import dev.brunofelix.tenancy.domain.enums.Plan;
import jakarta.transaction.Transactional;
import java.time.Clock;
import java.util.Objects;
import java.util.UUID;

public class AuthService {

  private static final String INVALID_CREDENTIALS = "E-mail ou senha inválidos";

  private final UserStore userStore;
  private final IdGenerator ids;
  private final PasswordHasher passwordHasher;
  private final TenantStore tenantStore;
  private final MembershipStore membershipStore;
  private final ProfileStore profileStore;
  private final Clock clock;

  public AuthService(
      IdGenerator ids,
      UserStore userStore,
      PasswordHasher passwordHasher,
      TenantStore tenantStore,
      MembershipStore membershipStore,
      ProfileStore profileStore,
      Clock clock) {
    this.ids = ids;
    this.userStore = userStore;
    this.passwordHasher = passwordHasher;
    this.tenantStore = tenantStore;
    this.membershipStore = membershipStore;
    this.profileStore = profileStore;
    this.clock = clock;
  }

  @Transactional
  public void signup(
      final String email, final String password, final String fullName, final String companyName) {
    var emailNormalized = Email.of(email);

    if (userStore.findByEmail(emailNormalized).isPresent()) {
      throw new IllegalArgumentException("E-mail já está em uso");
    }

    var user = User.register(ids.generate(), emailNormalized, passwordHasher.hash(password));
    userStore.insert(user);

    Tenant tenant = Tenant.create(ids.generate(), Name.of(companyName), Plan.BASIC);
    tenantStore.insert(tenant);

    var profile = Profile.create(user.id(), Name.of(fullName));
    profileStore.insert(profile);

    var membership = Membership.create(tenant.id(), user.id());
    membershipStore.insert(membership);
  }

  @Transactional
  public void verifyEmail(final UUID userId) {
    User user = findUser(userId);

    user.verifyEmail();
    userStore.update(user);
  }

  @Transactional
  public UUID authenticate(final String email, final String password) {
    Email normalizedEmail = Email.of(email);
    User user =
        userStore
            .findByEmail(normalizedEmail)
            .orElseThrow(() -> new IllegalArgumentException(INVALID_CREDENTIALS));

    if (!passwordHasher.matches(password, user.passwordHash())) {
      throw new IllegalArgumentException(INVALID_CREDENTIALS);
    }

    user.registerLogin(clock.instant());
    userStore.update(user);
    return user.id();
  }

  @Transactional
  public void changePassword(
      final UUID userId, final String currentPassword, final String newPassword) {
    User user = findUser(userId);

    if (!passwordHasher.matches(currentPassword, user.passwordHash())) {
      throw new IllegalArgumentException(INVALID_CREDENTIALS);
    }

    user.changePassword(passwordHasher.hash(newPassword));
    userStore.update(user);
  }

  private User findUser(final UUID userId) {
    Objects.requireNonNull(userId, "O identificador do usuário não pode ser nulo");
    return userStore
        .findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado"));
  }
}
