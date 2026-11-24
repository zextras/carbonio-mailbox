// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.soap.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

final class CharacterizationSnapshots {

  private static final Path ROOT = Path.of("src/test/resources/com/zimbra/soap/json/characterization");

  private final Path directory;

  CharacterizationSnapshots(String folder) {
    this.directory = ROOT.resolve(folder);
  }

  boolean exists(String fileName) {
    return Files.exists(directory.resolve(fileName));
  }

  String read(String fileName) throws IOException {
    return Files.readString(directory.resolve(fileName), StandardCharsets.UTF_8);
  }

  void write(String fileName, String content) throws IOException {
    Path snapshot = directory.resolve(fileName);
    Files.createDirectories(snapshot.getParent());
    Files.writeString(snapshot, content, StandardCharsets.UTF_8);
  }

  void assertMatches(String fileName, String actual, String message) throws IOException {
    if (!exists(fileName)) {
      write(fileName, actual);
      fail("snapshot created, review and commit it: " + directory.resolve(fileName));
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
