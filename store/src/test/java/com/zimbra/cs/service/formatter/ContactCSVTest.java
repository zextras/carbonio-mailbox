// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.formatter;

import com.google.common.base.Strings;
import java.io.BufferedReader;
import java.io.StringReader;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link ContactCSV}. Note that successful imports for ContactCSV will require
 * /opt/zextras/conf/contact-fields.xml Better to have any tests requiring that in
 * qa.unittest.TestContactCSV
 *
 * @author ysasaki
 */
public final class ContactCSVTest {

  @Test
  public void invalidFormat() throws Exception {
    StringReader reader = new StringReader(Strings.repeat("a\tb\tc", 100));
    try {
      ContactCSV.getContacts(new BufferedReader(reader), null);
      Assert.fail();
    } catch (ContactCSV.ParseException e) {
      Assert.assertEquals(
          "invalid format - header field 1 of 1 is too long (length=500)", e.getMessage());
    }
  }
}
