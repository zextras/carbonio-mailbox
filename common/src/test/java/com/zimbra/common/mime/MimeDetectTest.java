// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import static org.junit.jupiter.api.Assertions.*;

import com.zimbra.common.localconfig.LC;
import com.zimbra.common.cli.ExitCodeException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class MimeDetectTest {

  private static String BASE_MIME_MAGIC_DIRECTORY_PATH;

  @BeforeAll
  static void setup() {
    BASE_MIME_MAGIC_DIRECTORY_PATH = MimeDetectTest.class.getResource("/").getPath();
    LC.shared_mime_info_globs.setDefault(BASE_MIME_MAGIC_DIRECTORY_PATH + "globs2");
    LC.shared_mime_info_magic.setDefault(BASE_MIME_MAGIC_DIRECTORY_PATH + "magic");
  }

  @AfterAll
  static void cleanup() {
    LC.shared_mime_info_globs.setDefault("");
    LC.shared_mime_info_magic.setDefault("");
  }

  @Test
  void testFileName() throws IOException {
    MimeDetect.getMimeDetect().addGlob("image/jpeg", "*.jpg", 50);
    assertEquals("image/jpeg", MimeDetect.getMimeDetect().detect("2011.07.19 089+.JPG"));
    assertEquals("image/jpeg", MimeDetect.getMimeDetect().detect("2011.07.18 706+.jpg"));
    assertEquals("image/jpeg", MimeDetect.getMimeDetect().detect("2011.07.18 706+.jPg"));
  }

  private static void assertCommandExits0(Executable executable) {
    final ExitCodeException exitCodeException = assertThrows(ExitCodeException.class, executable);
    Assertions.assertEquals(0, exitCodeException.getExitCode());
  }

  @Test
  void testMain() throws Exception {
    try (ByteArrayOutputStream outContent = new ByteArrayOutputStream()) {
      System.setOut(new PrintStream(outContent));

      // with file path
      assertCommandExits0(() -> MimeDetect.run(new String[]{"path/2011.07.18 706+.txt"}));
      assertEquals("text/plain\n", outContent.toString());

      // with name
      outContent.reset();
      assertCommandExits0(() -> MimeDetect.run(new String[]{"2011.07.18 706+.png"}));
      assertEquals("image/png\n", outContent.toString());

      // with -g (globs file)
      outContent.reset();
      assertCommandExits0(
          () -> MimeDetect.run(new String[]{"-g", BASE_MIME_MAGIC_DIRECTORY_PATH + "globs2", "2011.07.18 706+.png"}));
      assertEquals("image/png\n", outContent.toString());

      // with -m (magic file)
      outContent.reset();
      assertCommandExits0(
          () -> MimeDetect.run(new String[]{"-m", BASE_MIME_MAGIC_DIRECTORY_PATH + "magic", "2011.07.18 706+.png"}));
      assertEquals("image/png\n", outContent.toString());

      // with -n (name only)
      outContent.reset();
      assertCommandExits0(
          () -> MimeDetect.run(new String[]{"-n", "2011.07.18 706+.mp4"}));
      assertEquals("video/mp4\n", outContent.toString());

      // with -g (globs file) and -n (name only)
      outContent.reset();
      assertCommandExits0(
          () -> MimeDetect.run(
              new String[]{"-g", BASE_MIME_MAGIC_DIRECTORY_PATH + "globs2", "-n", "2011.07.18 706+.eml"}));
      assertEquals("message/rfc822\n", outContent.toString());

      // with -v (validate extension and data)
      outContent.reset();
      assertCommandExits0(
          () -> MimeDetect.run(new String[]{"-v", BASE_MIME_MAGIC_DIRECTORY_PATH + "common-passwords.txt"}));
      assertEquals("text/plain\n", outContent.toString());
    }
  }
}