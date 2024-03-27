// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.util.MailMessageBuilder;
import com.zextras.mailbox.util.MailboxTestUtil.AccountAction;
import com.zimbra.common.account.Key;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapProtocol;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.ACLUtil;
import com.zimbra.cs.account.accesscontrol.GranteeType;
import com.zimbra.cs.account.accesscontrol.Right;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.account.accesscontrol.RightModifier;
import com.zimbra.cs.account.accesscontrol.ZimbraACE;
import com.zimbra.cs.mailbox.ACL;
import com.zimbra.cs.mailbox.Folder;
import com.zimbra.cs.mailbox.MailServiceException;
import com.zimbra.cs.mailbox.Mailbox;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.cs.mime.ParsedMessage;
import com.zimbra.cs.service.AuthProvider;
import com.zimbra.soap.SoapEngine;
import com.zimbra.soap.ZimbraSoapContext;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SendMsgIT {

  private static GreenMail greenMail;

  @BeforeAll
  public static void setUp() throws Exception {
    MailboxTestUtil.initServer();

    // Setup SMTP
    greenMail =
        new GreenMail(
            new ServerSetup[] {
              new ServerSetup(
                  SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();

    Provisioning prov = Provisioning.getInstance();
    final Account sharedAcct =
        prov.createAccount(
            "shared@test.com",
            "secret",
            new HashMap<>() {
              {
                put(ZAttrProvisioning.A_zimbraId, UUID.randomUUID().toString());
              }
            });
    final Account delegated =
        prov.createAccount("delegated@test.com", "secret", new HashMap<String, Object>());
    // Grant sendAs to delegated@
    final Set<ZimbraACE> aces =
        new HashSet<>() {
          {
            add(
                new ZimbraACE(
                    delegated.getId(),
                    GranteeType.GT_USER,
                    RightManager.getInstance().getRight(Right.RT_sendAs),
                    RightModifier.RM_CAN_DELEGATE,
                    null));
          }
        };
    ACLUtil.grantRight(Provisioning.getInstance(), sharedAcct, aces);
    // Grant shared@ root folder access to delegated@
    final short rwidx = ACL.stringToRights("rwidx");
    final Mailbox sharedAcctMailbox = MailboxManager.getInstance().getMailboxByAccount(sharedAcct);
    final Folder rootSharedAcctFolder = sharedAcctMailbox.getFolderByPath(null, "/");
    sharedAcctMailbox.grantAccess(
        null,
        rootSharedAcctFolder.getFolderId(),
        delegated.getId(),
        ACL.GRANTEE_AUTHUSER,
        rwidx,
        null);
  }

  @AfterAll
  public static void tearDown() {
    greenMail.stop();
  }

  private Message createDraft(String sender) throws Exception {
    Account account = Provisioning.getInstance().get(Key.AccountBy.name, sender);
    final ParsedMessage message = new MailMessageBuilder()
        .from(sender)
        .addRecipient(sender)
        .build();
    return AccountAction.Factory.getDefault().forAccount(account).saveDraft(message);
  }

  /**
   * CO-782: Test if Draft is saved in authed user's mailbox, and msg sent as shared account, then
   * user's Draft is deleted.
   *
   * @throws Exception
   */
  @Test
  void shouldDeleteDraftFromAuthAcctMailboxWhenSendingMailAsSharedAccount() throws Exception {
    final Account delegatedAcct =
        Provisioning.getInstance().get(Key.AccountBy.name, "delegated@test.com");

    final Account sharedAcct =
        Provisioning.getInstance().get(Key.AccountBy.name, "shared@test.com");

    final Message draft = createDraft(delegatedAcct.getName());
    // easiest way to create a message, sorry
    final String requestBody =
        String.format(
            "{\"m\":{\"did\":\"%d\",\"id\":\"%d\",\"su\":{\"_content\":\"AAA\"},\"e\":[{\"t\":\"f\",\"a\":\"shared@test.com\",\"d\":\"Test"
                + " Shared\"},{\"t\":\"t\",\"a\":\"recipient@test.com\"}],"
                + "\"mp\":[{\"ct\":\"multipart/alternative\","
                + "\"mp\":[{\"ct\":\"text/html\",\"body\":true,\"content\":{\"_content\":\"<html><body><div"
                + " style=\\\"font-family: arial, helvetica, sans-serif; font-size: 12pt; color:"
                + " #000000\\\"><p>Test</p></div></body></html>\"}},"
                + "{\"ct\":\"text/plain\",\"content\":{\"_content\":\"Test\"}}]}]}}}",
            draft.getId(), draft.getId());
    final Element jsonElement = Element.parseJSON(requestBody);

    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(delegatedAcct),
            sharedAcct.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    new SendMsg().handle(jsonElement, context);
    final Mailbox delegatedMbox = MailboxManager.getInstance().getMailboxByAccount(delegatedAcct);
    final MailServiceException receivedException =
        assertThrows(
            MailServiceException.class, () -> delegatedMbox.getMessageById(null, draft.getId()));

    // check draft is deleted after successful send
    assertEquals(MailServiceException.NO_SUCH_MSG, receivedException.getCode());
    assertEquals("no such message: " + draft.getId(), receivedException.getMessage());
  }

  /**
   * CO-782: Tests if a draft is created in a shared mailbox, when msg sent as shared account, then
   * the Draft is not deleted from shared mailbox.
   *
   * @throws Exception
   */
  @Test
  void shouldNotDeleteDraftFromSharedMailboxWhenSendingMailAsSharedAccount() throws Exception {
    final Account delegatedAcct =
        Provisioning.getInstance().get(Key.AccountBy.name, "delegated@test.com");

    final Account sharedAcct =
        Provisioning.getInstance().get(Key.AccountBy.name, "shared@test.com");

    final Message draft = createDraft(sharedAcct.getName());
    // easiest way to create a message, sorry
    final String requestBody =
        String.format(
            "{\"m\":{\"did\":\"%d\",\"id\":\"%d\",\"su\":{\"_content\":\"AAA\"},\"e\":[{\"t\":\"f\",\"a\":\"shared@test.com\",\"d\":\"Test"
                + " Shared\"},{\"t\":\"t\",\"a\":\"recipient@test.com\"}],"
                + "\"mp\":[{\"ct\":\"multipart/alternative\","
                + "\"mp\":[{\"ct\":\"text/html\",\"body\":true,\"content\":{\"_content\":\"<html><body><div"
                + " style=\\\"font-family: arial, helvetica, sans-serif; font-size: 12pt; color:"
                + " #000000\\\"><p>Test</p></div></body></html>\"}},"
                + "{\"ct\":\"text/plain\",\"content\":{\"_content\":\"Test\"}}]}]}}}",
            draft.getId(), draft.getId());
    final Element jsonElement = Element.parseJSON(requestBody);

    Map<String, Object> context = new HashMap<String, Object>();
    ZimbraSoapContext zsc =
        new ZimbraSoapContext(
            AuthProvider.getAuthToken(delegatedAcct),
            sharedAcct.getId(),
            SoapProtocol.Soap12,
            SoapProtocol.Soap12);
    context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
    new SendMsg().handle(jsonElement, context);
    final Mailbox sharedMbox = MailboxManager.getInstance().getMailboxByAccount(sharedAcct);
    final Message draftStillThere = sharedMbox.getMessageById(null, draft.getId());
    assertEquals(draft, draftStillThere);
  }
}
