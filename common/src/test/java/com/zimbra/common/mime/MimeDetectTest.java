// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.zimbra.common.localconfig.LC;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MimeDetectTest {
  private static final String BASE_MIME_MAGIC_DIRECTORY_PATH = MimeDetectTest.class.getResource("/").getPath();

  @BeforeAll
  static void setup(){
    LC.shared_mime_info_globs.setDefault(BASE_MIME_MAGIC_DIRECTORY_PATH+"globs2");
    LC.shared_mime_info_magic.setDefault(BASE_MIME_MAGIC_DIRECTORY_PATH + "globs2");
  }

  @AfterAll
  static void cleanup() {
    LC.shared_mime_info_globs.setDefault("");
    LC.shared_mime_info_globs.setDefault("");
  }

  @Test
  void testFileName() throws IOException {
    MimeDetect.getMimeDetect().addGlob("image/jpeg", "*.jpg", 50);
    assertEquals("image/jpeg", MimeDetect.getMimeDetect().detect("2011.07.19 089+.JPG"));
    assertEquals("image/jpeg", MimeDetect.getMimeDetect().detect("2011.07.18 706+.jpg"));
    assertEquals("image/jpeg", MimeDetect.getMimeDetect().detect("2011.07.18 706+.jPg"));
  }
}
