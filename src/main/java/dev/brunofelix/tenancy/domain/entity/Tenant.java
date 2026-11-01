package dev.brunofelix.tenancy.domain.entity;

import dev.brunofelix.shared.domain.valueobject.Name;
import dev.brunofelix.tenancy.domain.enums.Plan;
import dev.brunofelix.tenancy.domain.valueobject.Address;
import dev.brunofelix.tenancy.domain.valueobject.Phone;
import java.util.Objects;
import java.util.UUID;

public final class Tenant {

  private final UUID id;
  private Name name;
  private String document;
  private String email;
  private Phone phone;
  private Address address;
  private Plan plan;
  private boolean active;
  private boolean subscriptionActive;

  private Tenant(
      final UUID id,
      final Name name,
      final String document,
      final String email,
      final Phone phone,
      final Address address,
      final Plan plan,
      final boolean active,
      final boolean subscriptionActive) {
    this.id = Objects.requireNonNull(id, "O identificador do tenant é obrigatório");
    this.name = Objects.requireNonNull(name, "O nome do tenant é obrigatório");
    this.document = normalizeOptional(document);
    this.email = normalizeOptional(email);
    this.phone = phone;
    this.address = address;
    this.plan = Objects.requireNonNull(plan, "O plano do tenant é obrigatório");
    this.active = active;
    this.subscriptionActive = subscriptionActive;
  }

  public static Tenant create(final UUID id, final Name name, final Plan plan) {
    return new Tenant(id, name, null, null, null, null, plan, true, false);
  }

  public static Tenant restore(
      final UUID id,
      final Name name,
      final String document,
      final String email,
      final Phone phone,
      final Address address,
      final Plan plan,
      final boolean active,
      final boolean subscriptionActive) {
    return new Tenant(id, name, document, email, phone, address, plan, active, subscriptionActive);
  }

  public void rename(final Name name) {
    this.name = Objects.requireNonNull(name, "O nome do tenant é obrigatório");
  }

  public void changeContact(final String email, final Phone phone) {
    this.email = normalizeOptional(email);
    this.phone = phone;
  }

  public void changeAddress(final Address address) {
    this.address = address;
  }

  public void changeDocument(final String document) {
    this.document = normalizeOptional(document);
  }

  public void changePlan(final Plan plan) {
    this.plan = Objects.requireNonNull(plan, "O plano do tenant é obrigatório");
  }

  public void activate() {
    active = true;
  }

  public void deactivate() {
    active = false;
    subscriptionActive = false;
  }

  public void activateSubscription() {
    if (!active) {
      throw new IllegalStateException("Um tenant inativo não pode ativar a assinatura");
    }
    subscriptionActive = true;
  }

  public void deactivateSubscription() {
    subscriptionActive = false;
  }

  private static String normalizeOptional(final String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  public UUID id() {
    return id;
  }

  public Name name() {
    return name;
  }

  public String document() {
    return document;
  }

  public String email() {
    return email;
  }

  public Phone phone() {
    return phone;
  }

  public Address address() {
    return address;
  }

  public Plan plan() {
    return plan;
  }

  public boolean isActive() {
    return active;
  }

  public boolean isSubscriptionActive() {
    return subscriptionActive;
  }
}
