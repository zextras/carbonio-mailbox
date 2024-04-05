// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.tracking;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TrackingUtilTest {

  @Test
  void anonymize_shouldGenerateDifferentHashesForDifferentValues() {
    assertNotEquals(
        TrackingUtil.anonymize("a"),
        TrackingUtil.anonymize("b")
    );
  }
}