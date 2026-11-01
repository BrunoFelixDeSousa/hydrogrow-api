package dev.brunofelix.shared.infrastructure;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedEpochGenerator;
import dev.brunofelix.shared.service.IdGenerator;
import java.util.Objects;
import java.util.UUID;

public final class UuidV7Generator implements IdGenerator {

  private final TimeBasedEpochGenerator generator;

  public UuidV7Generator() {
    this(Generators.timeBasedEpochGenerator());
  }

  UuidV7Generator(final TimeBasedEpochGenerator generator) {
    this.generator = Objects.requireNonNull(generator, "O gerador UUID v7 é obrigatório");
  }

  @Override
  public UUID generate() {
    return generator.generate();
  }
}
