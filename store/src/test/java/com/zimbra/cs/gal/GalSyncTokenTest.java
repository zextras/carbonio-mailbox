// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.gal;

import com.zimbra.common.service.ServiceException;
import org.junit.Assert;
import org.junit.Test;

public class GalSyncTokenTest {

  @Test
  public void testSyncTokenTimestampZulu() throws ServiceException {
    String dateWithHour = "20150610220447";
    String fullTimestamp = dateWithHour + ".000Z";
    GalSyncToken token = new GalSyncToken(fullTimestamp);
    Assert.assertEquals(fullTimestamp, token.getLdapTimestamp());
    Assert.assertEquals(dateWithHour + "Z", token.getLdapTimestamp("yyyyMMddHHmmss'Z'"));
  }

  @Test
  public void testSyncTokenMerge() throws ServiceException {
    String t1 = "20180131045916.000Z:b1010a37-e08d-45d4-b69b-1ea411a75138:11";
    String t2 = "20180131045916.000Z:b1010a37-e08d-45d4-b69b-1ea411a75138:12";
    GalSyncToken token1 = new GalSyncToken(t1);
    GalSyncToken token2 = new GalSyncToken(t2);
    token2.merge(token1);
    Assert.assertEquals("GalSync tokens not merged correctly.", t2, token2.toString());
    t1 = "20180131045916.000Z:b1010a37-e08d-45d4-b69b-1ea411a75138:13";
    t2 = "20180131045916.000Z:b1010a37-e08d-45d4-b69b-1ea411a75138:14";
    token1 = new GalSyncToken(t1);
    token2 = new GalSyncToken(t2);
    token1.merge(token2);
    Assert.assertEquals("GalSync tokens not merged correctly.", t2, token1.toString());
  }
}
