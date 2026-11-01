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

public class AuthService {

  private final UserStore userStore;
  private final IdGenerator ids;
  private final PasswordHasher passwordHasher;
  private final TenantStore tenantStore;
  private final MembershipStore membershipStore;
  private final ProfileStore profileStore;

  public AuthService(
      IdGenerator ids,
      UserStore userStore,
      PasswordHasher passwordHasher,
      TenantStore tenantStore,
      MembershipStore membershipStore,
      ProfileStore profileStore) {
    this.ids = ids;
    this.userStore = userStore;
    this.passwordHasher = passwordHasher;
    this.tenantStore = tenantStore;
    this.membershipStore = membershipStore;
    this.profileStore = profileStore;
  }

  @Transactional
  public void signup(String email, String password, String fullName, String companyName) {
    var emailNormalized = Email.of(email);

    if (userStore.emailExists(emailNormalized)) {
      throw new IllegalArgumentException("Email já está em uso");
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
}
