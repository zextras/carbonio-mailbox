// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zextras.mailbox.hardlinks;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

public final class HardLink {

  private HardLink() {}

  /**
   * Creates a hard link from {@code oldpath} to {@code newpath}.
   */
  public static void link(String oldpath, String newpath) throws IOException {
    link(Path.of(oldpath), Path.of(newpath));
  }

  /**
   * Creates a hard link from {@code oldpath} to {@code newpath}.
   */
  public static void link(Path oldpath, Path newpath) throws IOException {
    try {
      Files.createLink(newpath, oldpath);
    } catch (NoSuchFileException e) {
      throw new FileNotFoundException(
              String.format("link(%s, %s): %s", oldpath, newpath, e.getMessage()));
    } catch (FileAlreadyExistsException e) {
      throw new IOException(
              String.format("link(%s, %s): %s", oldpath, newpath, e.getMessage()), e);
    }
  }
}
