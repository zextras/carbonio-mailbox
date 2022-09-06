// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.util;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import java.util.HashMap;
import java.util.Map;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JWEUtilTest {

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

  @Test
  public void testJWE() {
    Map<String, String> map = new HashMap<>();
    String val1 = "jwt";
    String val2 = "encryption";
    map.put("key1", val1);
    map.put("key2", val2);
    try {
      String jwe = JWEUtil.getJWE(map);
      Map<String, String> result = JWEUtil.getDecodedJWE(jwe);
      Assert.assertEquals(val1, result.get("key1"));
      Assert.assertEquals(val2, result.get("key2"));
    } catch (ServiceException se) {
      Assert.fail("testJWE failed");
    }
  }
}
