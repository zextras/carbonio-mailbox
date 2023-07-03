// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index;

import java.util.EnumSet;

import org.junit.jupiter.api.Test;

import com.zimbra.common.soap.Element;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.zimbra.common.soap.Element.XMLElement;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.service.util.ItemId;

/**
 * Unit test for {@link MsgQueryResults}.
 *
 * @author ysasaki
 */
public final class MsgQueryResultsTest {

 @Test
 void merge() throws Exception {
  MockQueryResults top = new MockQueryResults(EnumSet.of(MailItem.Type.MESSAGE), SortBy.NONE);
  top.add(new MessageHit(top, null, 1000, null, null, 0));
  top.add(new MessagePartHit(top, null, 1000, null, null, 0));
  top.add(new MessagePartHit(top, null, 1000, null, null, 0));
  top.add(new MessageHit(top, null, 1001, null, null, 0));
  top.add(new MessageHit(top, null, 1001, null, null, 0));
  top.add(new MessagePartHit(top, null, 1001, null, null, 0));
  top.add(new MessagePartHit(top, null, 1001, null, null, 0));
  top.add(new MessageHit(top, null, 1002, null, null, 0));
  top.add(new MessageHit(top, null, 1003, null, null, 0));

  ProxiedHit phit = new ProxiedHit(top, null, 0);
  phit.setParsedItemId(new ItemId("A", 1000));
  top.add(phit);

  phit = new ProxiedHit(top, null, 0);
  phit.setParsedItemId(new ItemId("B", 1000));
  top.add(phit);

  MsgQueryResults result = new MsgQueryResults(top, null, SortBy.NONE, SearchParams.Fetch.NORMAL);

  ZimbraHit hit = result.getNext();
  assertEquals(hit.getClass(), MessageHit.class);
  assertEquals(hit.getItemId(), 1000);

  hit = result.getNext();
  assertEquals(hit.getClass(), MessageHit.class);
  assertEquals(hit.getItemId(), 1001);

  hit = result.getNext();
  assertEquals(hit.getClass(), MessageHit.class);
  assertEquals(hit.getItemId(), 1002);

  hit = result.getNext();
  assertEquals(hit.getClass(), MessageHit.class);
  assertEquals(hit.getItemId(), 1003);

  hit = result.getNext();
  assertEquals(hit.getClass(), ProxiedHit.class);
  assertEquals(hit.getItemId(), 1000);

  hit = result.getNext();
  assertEquals(hit.getClass(), ProxiedHit.class);
  assertEquals(hit.getItemId(), 1000);

  assertFalse(result.hasNext());
 }

 @Test
 void proxiedHitNotMerged() throws Exception {
  MockQueryResults top = new MockQueryResults(EnumSet.of(MailItem.Type.MESSAGE), SortBy.NONE);
  top.add(new MessageHit(top, null, 1000, null, null, 0));

  Element el = XMLElement.create(SoapProtocol.Soap12, "hit");
  el.addAttribute(MailConstants.A_ID, 1000);
  top.add(new ProxiedHit(top, el, 0));

  MsgQueryResults result = new MsgQueryResults(top, null, SortBy.NONE, SearchParams.Fetch.NORMAL);

  ZimbraHit hit = result.getNext();
  assertEquals(hit.getClass(), MessageHit.class);
  assertEquals(hit.getItemId(), 1000);

  hit = result.getNext();
  assertEquals(hit.getClass(), ProxiedHit.class);
  assertEquals(hit.getItemId(), 1000);
 }

}
