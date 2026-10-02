package dev.brunofelix.identity.application.service;

import dev.brunofelix.identity.application.port.ProfileStore;
import dev.brunofelix.identity.application.port.UserStore;
import dev.brunofelix.identity.domain.entity.Profile;
import dev.brunofelix.identity.domain.entity.User;
import dev.brunofelix.identity.domain.service.PasswordHasher;
import dev.brunofelix.identity.domain.valueobject.Email;
import dev.brunofelix.identity.domain.valueobject.ProfileId;
import dev.brunofelix.identity.domain.valueobject.UserId;
import dev.brunofelix.shared.domain.valueobject.Name;
import dev.brunofelix.shared.service.IdGenerator;
import dev.brunofelix.tenancy.application.port.TenantMemberShipStore;
import dev.brunofelix.tenancy.application.port.TenantStore;
import dev.brunofelix.tenancy.domain.entity.Tenant;
import dev.brunofelix.tenancy.domain.entity.TenantMemberShip;
import dev.brunofelix.tenancy.domain.valueobject.TenantId;
import dev.brunofelix.tenancy.domain.valueobject.TenantMembershipId;
import jakarta.transaction.Transactional;
import java.time.Instant;

public class AuthService {

  private final UserStore userStore;
  private final IdGenerator ids;
  private final PasswordHasher passwordHasher;
  private final TenantStore tenantStore;
  private final TenantMemberShipStore tenantMemberShipStore;
  private final ProfileStore profileStore;

  public AuthService(
      IdGenerator ids,
      UserStore userStore,
      PasswordHasher passwordHasher,
      TenantStore tenantStore,
      TenantMemberShipStore tenantMemberShipStore,
      ProfileStore profileStore) {
    this.ids = ids;
    this.userStore = userStore;
    this.passwordHasher = passwordHasher;
    this.tenantStore = tenantStore;
    this.tenantMemberShipStore = tenantMemberShipStore;
    this.profileStore = profileStore;
  }

  @Transactional
  public void signup(String email, String password, String fullName, String companyName) {
    var emailNormalized = Email.of(email);

    if (userStore.emailExists(emailNormalized)) {
      throw new IllegalArgumentException("Email já está em uso");
    }

    var user =
        User.create(UserId.of(ids.generate()), emailNormalized, passwordHasher.hash(password));

    user.verifyEmail();
    userStore.insert(user);

    var now = Instant.now();

    Tenant tenant = Tenant.builder(TenantId.of(ids.generate()), Name.of(companyName), now).build();
    tenantStore.insert(tenant);

    var profile = Profile.create(ProfileId.of(ids.generate()), user.id(), Name.of(fullName), now);
    profileStore.insert(profile);

    var tenantMemberShip =
        TenantMemberShip.create(TenantMembershipId.of(ids.generate()), tenant.id(), user.id(), now);
    tenantMemberShipStore.insert(tenantMemberShip);
  }
}
