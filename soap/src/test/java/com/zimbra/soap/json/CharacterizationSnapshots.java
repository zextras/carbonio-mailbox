// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class CharacterizationSnapshots {

  private static final Path ROOT = Path.of("src/test/resources/com/zimbra/soap/json/characterization");
  private static final String UPDATE_PROPERTY = "characterization.update";

  private final Path directory;

  CharacterizationSnapshots(String folder) {
    this.directory = ROOT.resolve(folder);
  }

  boolean isUpdating() {
    return Boolean.getBoolean(UPDATE_PROPERTY);
  }

  boolean exists(String fileName) {
    return Files.exists(directory.resolve(fileName));
  }

  String read(String fileName) throws IOException {
    Path snapshot = directory.resolve(fileName);
    assertTrue(Files.exists(snapshot), "missing snapshot, run with -D" + UPDATE_PROPERTY + "=true: " + snapshot);
    return Files.readString(snapshot, StandardCharsets.UTF_8);
  }

  void write(String fileName, String content) throws IOException {
    Path snapshot = directory.resolve(fileName);
    Files.createDirectories(snapshot.getParent());
    Files.writeString(snapshot, content, StandardCharsets.UTF_8);
  }

  void assertMatches(String fileName, String actual, String message) throws IOException {
    if (isUpdating()) {
      write(fileName, actual);
      return;
    }
    assertEquals(read(fileName), actual, message);
  }

  static String describeFailure(String action, Exception failure) {
    Throwable rootCause = failure;
    while (rootCause.getCause() != null) {
      rootCause = rootCause.getCause();
    }
    String firstLine = String.valueOf(rootCause.getMessage()).lines().findFirst().orElse("");
    return action + " fails: " + rootCause.getClass().getName() + ": " + firstLine + "\n";
  }
}
