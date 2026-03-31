// SPDX-FileCopyrightText: 2023 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.mail;

import static com.zimbra.cs.mailbox.MailServiceException.QUOTA_EXCEEDED;
import static org.junit.jupiter.api.Assertions.*;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.MailboxTestSuite;
import com.zextras.mailbox.util.AccountAction;
import com.zextras.mailbox.util.MailMessageBuilder;
import com.zimbra.common.account.ZAttrProvisioning;
import com.zimbra.common.service.ServiceException;
import com.zimbra.common.soap.Element;
import com.zimbra.common.soap.SoapParseException;
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
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SendMsgIT extends MailboxTestSuite {

  private static GreenMail greenMail;
  private static Account recipient;
  private static Account sharedAcct;
  private static Account delegatedAcct;
	private static final String TEST_RECIPIENT = "test@outside.com";

	@BeforeAll
  public static void setUp() throws Exception {
		recipient = createAccount().create();
    // Setup SMTP
    greenMail =
        new GreenMail(
            new ServerSetup[] {
              new ServerSetup(
                  SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
            });
    greenMail.start();

    sharedAcct = createAccount().create();
    delegatedAcct = createAccount().create();
    // Grant sendAs to delegated@
    final Set<ZimbraACE> aces =
        new HashSet<>() {
          {
            add(
                new ZimbraACE(
                    delegatedAcct.getId(),
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
        delegatedAcct.getId(),
        ACL.GRANTEE_AUTHUSER,
        rwidx,
        null);
  }

  @AfterAll
  public static void tearDown() {
    greenMail.stop();
  }

	@BeforeEach
	public void setup() throws Exception {
		greenMail.reset();
	}

  private Message saveDraftOnMailbox(Account account) throws Exception {
    final ParsedMessage message = new MailMessageBuilder()
        .from(account.getName())
        .addRecipient(TEST_RECIPIENT)
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
  void shouldDeleteDraftFromAuthenticatedAccountMailboxWhenSendingMailAsSharedAccount() throws Exception {
    final Message draftOnDelegatedAccount = saveDraftOnMailbox(delegatedAcct);
		final Element jsonElement = sendDraftRequest(draftOnDelegatedAccount, sharedAcct.getName(), TEST_RECIPIENT);

		final Map<String, Object> context = delegatedRequest(delegatedAcct, sharedAcct);
		new SendMsg().handle(jsonElement, context);

    final Mailbox delegatedMbox = MailboxManager.getInstance().getMailboxByAccount(delegatedAcct);
    final MailServiceException receivedException =
        assertThrows(
            MailServiceException.class, () -> delegatedMbox.getMessageById(null, draftOnDelegatedAccount.getId()));
    // check draft is deleted after successful send
    assertEquals(MailServiceException.NO_SUCH_MSG, receivedException.getCode());
    assertEquals("no such message: " + draftOnDelegatedAccount.getId(), receivedException.getMessage());
  }

	/**
   * CO-782: Tests if a draft is created in a shared mailbox, when msg sent as shared account, then
   * the Draft is not deleted from shared mailbox.
   *
   * @throws Exception
   */
  @Test
  void shouldNotDeleteDraftFromSharedMailboxWhenSendingMailAsSharedAccount() throws Exception {
    final Message draftOnSharedAccount = saveDraftOnMailbox(sharedAcct);
		final Element jsonElement = sendDraftRequest(draftOnSharedAccount, sharedAcct.getName(), TEST_RECIPIENT);

		final Map<String, Object> context = delegatedRequest(delegatedAcct, sharedAcct);
		new SendMsg().handle(jsonElement, context);

    final Mailbox sharedMbox = MailboxManager.getInstance().getMailboxByAccount(sharedAcct);
    final Message draftStillThere = sharedMbox.getMessageById(null, draftOnSharedAccount.getId());
    assertEquals(draftOnSharedAccount, draftStillThere);
  }

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void denySendInOverQuota_WhenSavingToSent(Boolean allowMailReceiveButNotSend) throws Exception {
		final Account account = createAccount().create();
		final Message draft = saveDraftOnMailbox(account);
		account.setMailQuota(1);
		account.setMailAllowReceiveButNotSendWhenOverQuota(allowMailReceiveButNotSend);

		var saveToSent = sendDraftRequest(draft, account.getName(), recipient.getName(), false);
		assertQuotaExceeded(() -> new SendMsg().handle(saveToSent, as(account)));
	}

	@Test
	void allowSendInOverQuota_WhenNotSavingToSent() throws Exception {
		final Account account = createAccount().create();
		final Message draft = saveDraftOnMailbox(account);
		account.setMailQuota(1);
		account.setMailAllowReceiveButNotSendWhenOverQuota(false);

		var noSaveToSent = sendDraftRequest(draft, account.getName(), recipient.getName(), true);
		new SendMsg().handle(noSaveToSent, as(account));

		assertEquals(1, greenMail.getReceivedMessages().length);
	}

	private void assertQuotaExceeded(Callable<Element> soapCall) {
		final MailServiceException mailServiceException = assertThrows(MailServiceException.class,
				soapCall::call);
		assertEquals(QUOTA_EXCEEDED, mailServiceException.getCode());
	}


	private static Element sendDraftRequest(Message draft, String sender, String recipient) throws SoapParseException {
		return sendDraftRequest(draft, sender, recipient, false);
	}
	private static Element sendDraftRequest(Message draft, String sender, String recipient, Boolean noSave) throws SoapParseException {
		final String requestBody =
				"""
				{"noSave": %b, "m":{"did":"%d","id":"%d","su":{"_content":"AAA"},
				"e":[{"t":"f","a":"%s","d":"Test Shared"},{"t":"t","a":"%s"}],
				"mp":[{"ct":"multipart/alternative",
				"mp":[{"ct":"text/html","body":true,
				"content":{"_content":"<html><body><div><p>Test</p></div></body></html>"}},
				{"ct":"text/plain","content":{"_content":"Test"}}]}]}}}
				""".formatted(noSave, draft.getId(), draft.getId(), sender, recipient);
		return Element.parseJSON(requestBody);
	}


	private static Map<String, Object> delegatedRequest(Account authenticated, Account requested) throws ServiceException {
		Map<String, Object> context = new HashMap<String, Object>();
		ZimbraSoapContext zsc =
				new ZimbraSoapContext(
						AuthProvider.getAuthToken(authenticated),
						requested.getId(),
						SoapProtocol.Soap12,
						SoapProtocol.Soap12);
		context.put(SoapEngine.ZIMBRA_CONTEXT, zsc);
		return context;
	}

	private static Map<String, Object> as(Account authenticated) throws ServiceException {
		return delegatedRequest(authenticated, authenticated);
	}
}
