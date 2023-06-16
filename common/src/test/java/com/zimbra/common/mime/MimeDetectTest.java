// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.mime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.jupiter.api.Test;

public class MimeDetectTest {

  @Test
  void testFileName() throws IOException {
    MimeDetect.getMimeDetect().addGlob("image/jpeg", "*.jpg", 50);
    assertEquals("image/jpeg", MimeDetect.getMimeDetect().detect("2011.07.19 089+.JPG"));
    assertEquals("image/jpeg", MimeDetect.getMimeDetect().detect("2011.07.18 706+.jpg"));
    assertEquals("image/jpeg", MimeDetect.getMimeDetect().detect("2011.07.18 706+.jPg"));
  }
}
