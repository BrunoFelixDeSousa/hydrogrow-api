package dev.brunofelix.shared.domain;

import java.util.Objects;
import java.util.regex.Pattern;

public final class EntityValidation {

  private static final Pattern STATE = Pattern.compile("^[A-Z]{2}$");
  private static final Pattern SHA_256 = Pattern.compile("^[0-9a-f]{64}$");

  private EntityValidation() {}

  public static <T> T required(T value, String message) {
    return Objects.requireNonNull(value, message);
  }

  public static String trimmedLength(String value, String field, int minimum, int maximum) {
    required(value, field + " é obrigatório");
    int length = value.trim().length();
    require(
        length >= minimum && length <= maximum,
        field + " deve ter entre " + minimum + " e " + maximum + " caracteres");
    return value;
  }

  public static String state(String value) {
    require(
        value == null || STATE.matcher(value).matches(),
        "O estado deve conter duas letras maiúsculas");
    return value;
  }

  public static String sha256(String value) {
    required(value, "O hash do token é obrigatório");
    require(
        SHA_256.matcher(value).matches(), "O hash do token deve ser SHA-256 hexadecimal minúsculo");
    return value;
  }

  private static void require(boolean condition, String message) {
    if (!condition) {
      throw new IllegalArgumentException(message);
    }
  }
}
