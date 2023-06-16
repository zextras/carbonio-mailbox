// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.gal;

import org.junit.jupiter.api.Test;

import com.zimbra.common.service.ServiceException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GalSyncTokenTest {

 @Test
 void testSyncTokenTimestampZulu() throws ServiceException {
  String dateWithHour = "20150610220447";
  String fullTimestamp = dateWithHour + ".000Z";
  GalSyncToken token = new GalSyncToken(fullTimestamp);
  assertEquals(fullTimestamp, token.getLdapTimestamp());
  assertEquals(dateWithHour + "Z", token.getLdapTimestamp("yyyyMMddHHmmss'Z'"));
 }

 @Test
 void testSyncTokenMerge() throws ServiceException {
  String t1 = "20180131045916.000Z:b1010a37-e08d-45d4-b69b-1ea411a75138:11";
  String t2 = "20180131045916.000Z:b1010a37-e08d-45d4-b69b-1ea411a75138:12";
  GalSyncToken token1 = new GalSyncToken(t1);
  GalSyncToken token2 = new GalSyncToken(t2);
  token2.merge(token1);
  assertEquals(t2, token2.toString(), "GalSync tokens not merged correctly.");
  t1 = "20180131045916.000Z:b1010a37-e08d-45d4-b69b-1ea411a75138:13";
  t2 = "20180131045916.000Z:b1010a37-e08d-45d4-b69b-1ea411a75138:14";
  token1 = new GalSyncToken(t1);
  token2 = new GalSyncToken(t2);
  token1.merge(token2);
  assertEquals(t2, token1.toString(), "GalSync tokens not merged correctly.");
 }
}
