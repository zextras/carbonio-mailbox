// SPDX-FileCopyrightText: 2026 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ByteSizeFormatterTest {

  @ParameterizedTest
  @CsvSource({
    "0, 0 B",
    "1, 1 B",
    "1023, 1023 B",
    "1024, 1.00 KB",
    "1536, 1.50 KB",
    "1048575, 1024.00 KB",
    "1048576, 1.00 MB",
    "1048576000, 1000.00 MB",
    "1073741824, 1.00 GB",
    "5242880000, 4.88 GB",
    "9223372036854775807, 8589934592.00 GB"
  })
  void shouldFormatSizeWithTheLargestFittingUnit(long bytes, String expected) {
    Assertions.assertEquals(expected, ByteSizeFormatter.format(bytes));
  }
}
