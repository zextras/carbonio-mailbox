// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import com.zimbra.common.account.Key;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.soap.JaxbUtil;
import com.zimbra.soap.mail.message.SaveDraftRequest;
import com.zimbra.soap.mail.message.SaveDraftResponse;
import com.zimbra.soap.mail.type.AttachmentsInfo;
import com.zimbra.soap.mail.type.MimePartAttachSpec;
import com.zimbra.soap.mail.type.MimePartInfo;
import com.zimbra.soap.mail.type.PartInfo;
import com.zimbra.soap.mail.type.SaveDraftMsg;
import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.mail.Address;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;

import javax.mail.internet.MimeMessage.RecipientType;
import javax.mail.internet.MimeMultipart;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.Maps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
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

public class SaveDraftTest {

    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
        Provisioning prov = Provisioning.getInstance();

        prov.createAccount("test@zimbra.com", "secret", Maps.<String, Object>newHashMap());
    }

    @BeforeEach
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
 void deleteRace() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");

  // create a draft via SOAP
  Element request = new Element.JSONElement(MailConstants.SAVE_DRAFT_REQUEST);
  Element m = request.addNonUniqueElement(MailConstants.E_MSG).addAttribute(MailConstants.E_SUBJECT, "dinner appt");
  m.addUniqueElement(MailConstants.E_MIMEPART).addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain").addAttribute(MailConstants.E_CONTENT, ORIGINAL_CONTENT);

  Element response = new SaveDraft() {
   @Override
   protected Element generateResponse(ZimbraSoapContext zsc, ItemIdFormatter ifmt, OperationContext octxt,
     Mailbox mbox, Message msg, boolean wantImapUid, boolean wantModSeq) {
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
  assertNull(response.getOptionalElement(MailConstants.E_MSG), "picked up delete");
 }

 @Test
 void updateRace() throws Exception {
  Account acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");

  // create a draft via SOAP
  Element request = new Element.JSONElement(MailConstants.SAVE_DRAFT_REQUEST);
  Element m = request.addNonUniqueElement(MailConstants.E_MSG).addAttribute(MailConstants.E_SUBJECT, "dinner appt");
  m.addUniqueElement(MailConstants.E_MIMEPART).addAttribute(MailConstants.A_CONTENT_TYPE, "text/plain").addAttribute(MailConstants.E_CONTENT, ORIGINAL_CONTENT);

  Element response = new SaveDraft() {
   @Override
   protected Element generateResponse(ZimbraSoapContext zsc, ItemIdFormatter ifmt, OperationContext octxt,
     Mailbox mbox, Message msg, boolean wantImapUid, boolean wantModSeq) {
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
    return super.generateResponse(zsc, ifmt, octxt, mbox, snapshotMsg, wantImapUid, wantModSeq);
   }
  }.handle(request, ServiceTestUtil.getRequestContext(acct));

  // make sure the response has the correct message content
  assertEquals(MODIFIED_CONTENT, response.getElement(MailConstants.E_MSG).getElement(MailConstants.E_MIMEPART).getAttribute(MailConstants.E_CONTENT), "picked up modified content");
 }


 @Test
 void smartLinkIsIncludedInSaveDraftResponse() throws Exception {
   final var acct = Provisioning.getInstance().getAccountByName("test@zimbra.com");

   var draftMessage = createDraftWithFileAttachment(acct);

   final var context = ServiceTestUtil.getRequestContext(acct);
   final var request = new SaveDraftRequest();
   final var message = new SaveDraftMsg();
   AttachmentsInfo attachments = new AttachmentsInfo();
   boolean requiresSmartLinkConversion = true;
   attachments.addAttachment(new MimePartAttachSpec(String.valueOf(draftMessage.getId()), "1", requiresSmartLinkConversion));
   message.setAttachments(attachments);
   message.setSubject("dinner appt");
   message.setContent("bee");

   request.setMsg(message);

   Element response = new SaveDraft().handle(JaxbUtil.jaxbToElement(request), context);
   SaveDraftResponse saveDraftResponse = JaxbUtil.elementToJaxb(response, SaveDraftResponse.class);
   PartInfo topLevelPartInfo = saveDraftResponse.getMessage().getContentElems().get(0);
   var attachmentPartInfo = topLevelPartInfo.getMimeParts().get(0);
   assertEquals(requiresSmartLinkConversion, attachmentPartInfo.getRequiresSmartLinkConversion());
 }

  /*
   *  Remove duplicated code, see com.zimbra.cs.service.mail.CopyToFilesIT#createDraftWithFileAttachment
   */
  private Message createDraftWithFileAttachment(Account account) throws Exception {
    final MimeMessage mimeMessage = new MimeMessage(Session.getInstance(new Properties()));
    Account acct = Provisioning.getInstance().get(Key.AccountBy.name, account.getName());
    final Mailbox mailbox = MailboxManager.getInstance().getMailboxByAccount(acct);
    final OperationContext operationContext = new OperationContext(acct);
    Address[] recipients = new Address[] {new InternetAddress(acct.getName())};
    mimeMessage.setFrom(new InternetAddress(acct.getName()));
    mimeMessage.setRecipients(RecipientType.TO, recipients);
    mimeMessage.setSubject("Test email");
    Multipart multipart = new MimeMultipart();
    MimeBodyPart text = new MimeBodyPart();
    text.setText("Hello there");
    MimeBodyPart attachmentPart = new MimeBodyPart();
    attachmentPart.attachFile(new File(this.getClass().getResource("/test-save-to-files.txt").getFile()));
    multipart.addBodyPart(text);
    multipart.addBodyPart(attachmentPart);
    mimeMessage.setContent(multipart);
    mimeMessage.setSender(new InternetAddress(acct.getName()));
    final ParsedMessage parsedMessage =
        new ParsedMessage(mimeMessage, mailbox.attachmentsIndexingEnabled());
    return mailbox.saveDraft(operationContext, parsedMessage, Mailbox.ID_AUTO_INCREMENT);
  }
}
