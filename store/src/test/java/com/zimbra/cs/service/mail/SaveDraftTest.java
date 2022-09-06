// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.google.common.collect.Maps;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.MailConstants;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailbox.MailItem;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailbox.OperationContext;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.util.ItemIdFormatter;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.ZimbraSoapContext;
import javax.mail.internet.MimeMessage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class SaveDraftTest {

  @BeforeClass
  public static void init() throws Exception {
    MailboxTestUtil.initServer();
    Provisioning prov = Provisioning.getInstance();

    prov.createAccount("test@zimbra.com", "secret", Maps.<String, Object>newHashMap());
  }

  @Before
  public void setUp() throws Exception {
    MailboxTestUtil.clearData();
  }

  private static String nCopiesOf(char c, int copies) {
    StringBuilder sb = new StringBuilder(copies);
    for (int i = 0; i < copies; i++) {
      sb.append(c);
    }
    return sb.toString();
  }

  // string lengths should be greater than both MessageCache.MESSAGE_CACHE_DISK_STREAMING_THRESHOLD
  //   and LC.zimbra_blob_input_stream_buffer_size_kb * 1024
  static final String ORIGINAL_CONTENT = nCopiesOf('a', 8192);
  static final String MODIFIED_CONTENT = nCopiesOf('b', 8192);

  @Test
  public void deleteRace() throws Exception {
    Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");

    // create a draft via SOAP
    Element request = new Element.JSONElement(MailConstants.SAVE_DRAFT_REQUEST);
    Element m =
        request
            .addNonUniqueElement(MailConstants.E_MSG)
            .addAttribute(MailConstants.E_SUBJECT, "dinner appt");
    m.addUniqueElement(MailConstants.E_MIMEPART)
        .addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain")
        .addAttribute(MailConstants.E_CONTENT, ORIGINAL_CONTENT);

    Element response =
        new SaveDraft() {
          @Override
          protected Element generateResponse(
              ZimbraSoapContext zsc,
              ItemIdFormatter ifmt,
              OperationContext octxt,
              Mailbox mbox,
              Message msg,
              boolean wantImapUid,
              boolean wantModSeq) {
            // trigger the failure case by deleting the draft before it's serialized out
            try {
              mbox.delete(null, msg.getId(), MailItem.Type.MESSAGE);
            } catch (Exception e) {
              return null;
            }
            return super.generateResponse(zsc, ifmt, octxt, mbox, msg, wantImapUid, wantModSeq);
          }
        }.handle(request, ServiceTestUtil.getRequestContext(acct));

    // make sure the response has no <m> element
    Assert.assertNull("picked up delete", response.getOptionalElement(MailConstants.E_MSG));
  }

  @Test
  public void updateRace() throws Exception {
    Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");

    // create a draft via SOAP
    Element request = new Element.JSONElement(MailConstants.SAVE_DRAFT_REQUEST);
    Element m =
        request
            .addNonUniqueElement(MailConstants.E_MSG)
            .addAttribute(MailConstants.E_SUBJECT, "dinner appt");
    m.addUniqueElement(MailConstants.E_MIMEPART)
        .addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain")
        .addAttribute(MailConstants.E_CONTENT, ORIGINAL_CONTENT);

    Element response =
        new SaveDraft() {
          @Override
          protected Element generateResponse(
              ZimbraSoapContext zsc,
              ItemIdFormatter ifmt,
              OperationContext octxt,
              Mailbox mbox,
              Message msg,
              boolean wantImapUid,
              boolean wantModSeq) {
            // trigger the failure case by re-saving the draft before it's serialized out
            Message snapshotMsg = msg;
            try {
              snapshotMsg = (Message) msg.snapshotItem();

              MimeMessage mm = new MimeMessage(JMSession.getSession());
              mm.setText(MODIFIED_CONTENT);
              mm.saveChanges();
              mbox.saveDraft(null, new ParsedMessage(mm, false), snapshotMsg.getId());
            } catch (Exception e) {
              return null;
            }
            return super.generateResponse(
                zsc, ifmt, octxt, mbox, snapshotMsg, wantImapUid, wantModSeq);
          }
        }.handle(request, ServiceTestUtil.getRequestContext(acct));

    // make sure the response has the correct message content
    Assert.assertEquals(
        "picked up modified content",
        MODIFIED_CONTENT,
        response
            .getElement(MailConstants.E_MSG)
            .getElement(MailConstants.E_MIMEPART)
            .getAttribute(MailConstants.E_CONTENT));
  }
}
