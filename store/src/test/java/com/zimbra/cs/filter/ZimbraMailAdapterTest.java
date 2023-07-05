// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zimbra.cs.filter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zimbra.cs.mailbox.DeliveryContext;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.store.Blob;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ZimbraMailAdapterTest {

  @BeforeAll
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
  }

 @Test
 void testUpdateIncomingBlob() throws Exception {
  int mboxId = 10;

  Mailbox mbox = mock(Mailbox.class);
  when(mbox.getId()).thenReturn(mboxId);

  List<Integer> targetMailboxIds = new ArrayList<Integer>(1);
  targetMailboxIds.add(mboxId);
  DeliveryContext sharedDeliveryCtxt = new DeliveryContext(true, targetMailboxIds);

  String testStr = "test";
  ParsedMessage pm = new ParsedMessage(testStr.getBytes(), false);
  IncomingMessageHandler handler = mock(IncomingMessageHandler.class);
  when(handler.getDeliveryContext()).thenReturn(sharedDeliveryCtxt);
  when(handler.getParsedMessage()).thenReturn(pm);
  ZimbraMailAdapter mailAdapter = new ZimbraMailAdapter(mbox, handler);
  mailAdapter.updateIncomingBlob();

  assertNotNull(sharedDeliveryCtxt.getMailBoxSpecificBlob(mboxId));
  assertNull(sharedDeliveryCtxt.getIncomingBlob());

  DeliveryContext nonSharedDeliveryCtxt = new DeliveryContext(false, targetMailboxIds);
  when(handler.getDeliveryContext()).thenReturn(nonSharedDeliveryCtxt);
  mailAdapter.updateIncomingBlob();

  assertNull(nonSharedDeliveryCtxt.getMailBoxSpecificBlob(mboxId));
  assertNotNull(nonSharedDeliveryCtxt.getIncomingBlob());

  when(handler.getDeliveryContext()).thenReturn(sharedDeliveryCtxt);
  Blob blobFile = sharedDeliveryCtxt.getMailBoxSpecificBlob(mboxId);
  mailAdapter.cloneParsedMessage();
  mailAdapter.updateIncomingBlob();
  assertNotNull(sharedDeliveryCtxt.getMailBoxSpecificBlob(mboxId));
  assertNull(sharedDeliveryCtxt.getIncomingBlob());
  assertNotSame(blobFile, sharedDeliveryCtxt.getMailBoxSpecificBlob(mboxId));

  when(handler.getDeliveryContext()).thenReturn(nonSharedDeliveryCtxt);
  blobFile = nonSharedDeliveryCtxt.getMailBoxSpecificBlob(mboxId);
  mailAdapter.cloneParsedMessage();
  mailAdapter.updateIncomingBlob();
  assertNull(nonSharedDeliveryCtxt.getMailBoxSpecificBlob(mboxId));
  assertNotNull(nonSharedDeliveryCtxt.getIncomingBlob());
  assertEquals(blobFile, nonSharedDeliveryCtxt.getMailBoxSpecificBlob(mboxId));
 }
}
