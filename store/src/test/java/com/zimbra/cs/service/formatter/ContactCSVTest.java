// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import java.io.BufferedReader;
import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.google.common.base.Strings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit test for {@link ContactCSV}.
 * Note that successful imports for ContactCSV will require /opt/zextras/conf/contact-fields.xml
 * Better to have any tests requiring that in com.zimbra.qa.unittest.TestContactCSV
 *
 * @author ysasaki
 */
public final class ContactCSVTest {

 @Test
 void invalidFormat() throws Exception {
  StringReader reader = new StringReader(Strings.repeat("a\tb\tc", 100));
  try {
   ContactCSV.getContacts(new BufferedReader(reader), null);
   fail();
  } catch (ContactCSV.ParseException e) {
   assertEquals("invalid format - header field 1 of 1 is too long (length=500)", e.getMessage());
  }
 }

}
