package dev.brunofelix.tenancy.domain.entity;

import dev.brunofelix.shared.domain.EntityValidation;
import dev.brunofelix.shared.domain.valueobject.Name;
import dev.brunofelix.tenancy.domain.valueobject.Address;
import dev.brunofelix.tenancy.domain.valueobject.Phone;
import dev.brunofelix.tenancy.domain.valueobject.TenantId;
import java.time.Instant;

public record Tenant(
    TenantId id,
    Name name,
    String document,
    String email,
    Phone phone,
    Address address,
    String plan,
    boolean isActive,
    boolean subscriptionActive,
    Instant createdAt,
    Instant updatedAt) {

  public Tenant {
    id = EntityValidation.required(id, "O identificador do tenant é obrigatório");
    name = EntityValidation.required(name, "O nome do tenant é obrigatório");
    plan = EntityValidation.required(plan, "O plano do tenant é obrigatório");
    createdAt = EntityValidation.required(createdAt, "A data de criação do tenant é obrigatória");
    updatedAt =
        EntityValidation.required(updatedAt, "A data de atualização do tenant é obrigatória");
  }

  public static Builder builder(final TenantId id, final Name name, final Instant createdAt) {
    return new Builder(id, name, createdAt);
  }

  public static final class Builder {

    private final TenantId id;
    private final Name name;
    private String document;
    private String email;
    private Phone phone;
    private Address address;
    private String plan = "BASIC";
    private boolean isActive = true;
    private boolean subscriptionActive;
    private final Instant createdAt;
    private Instant updatedAt;

    private Builder(final TenantId id, final Name name, final Instant createdAt) {
      this.id = EntityValidation.required(id, "O identificador do tenant é obrigatório");
      this.name = EntityValidation.required(name, "O nome do tenant é obrigatório");
      this.createdAt =
          EntityValidation.required(createdAt, "A data de criação do tenant é obrigatória");
      this.updatedAt = createdAt;
    }

    public Builder document(final String document) {
      this.document = document;
      return this;
    }

    public Builder email(final String email) {
      this.email = email;
      return this;
    }

    public Builder phone(final Phone phone) {
      this.phone = phone;
      return this;
    }

    public Builder address(final Address address) {
      this.address = address;
      return this;
    }

    public Builder plan(final String plan) {
      this.plan = EntityValidation.required(plan, "O plano do tenant é obrigatório");
      return this;
    }

    public Builder isActive(final boolean isActive) {
      this.isActive = isActive;
      return this;
    }

    public Builder subscriptionActive(final boolean subscriptionActive) {
      this.subscriptionActive = subscriptionActive;
      return this;
    }

    public Builder updatedAt(final Instant updatedAt) {
      this.updatedAt =
          EntityValidation.required(updatedAt, "A data de atualização do tenant é obrigatória");
      return this;
    }

    public Tenant build() {
      return new Tenant(
          id,
          name,
          document,
          email,
          phone,
          address,
          plan,
          isActive,
          subscriptionActive,
          createdAt,
          updatedAt);
    }
  }
}
