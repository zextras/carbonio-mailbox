// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.common.util;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class UUIDUtilTest {

  @Test
  void generatesParsableUniqueUUIDs() {
    String uuid = UUIDUtil.generateUUID();
    assertTrue(StringUtil.isUUID(uuid));
    assertNotEquals(uuid, UUIDUtil.generateUUID());
    UUID.fromString(uuid);
  }
}
