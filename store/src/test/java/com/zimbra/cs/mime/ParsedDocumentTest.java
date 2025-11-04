// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.mime;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class ParsedDocumentTest {

 @Test
 void test2GBFileSize() throws Exception {
  long size2GBMinus = 0x7fffffffL;
  long size2GB = 0x80000000L;

  // No truncation when converting <2GB long to int
  int int2GBMinus = (int) size2GBMinus;
  assertEquals(size2GBMinus, int2GBMinus);
  assertEquals("2147483647", Integer.toString(int2GBMinus));

  // Truncation when converting 2GB long to int (simulates error responsible for HS-5126)
  int int2GB = (int) size2GB;
  long negative2GB = -2147483648;
  assertEquals(negative2GB, int2GB);
  assertEquals("-2147483648", Integer.toString(int2GB));
 }
}
