/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zextras.mailbox.qa;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.AccountAction;
import com.zextras.mailbox.util.AccountCreator;
import com.zextras.mailbox.util.AccountCreator.Factory;
import com.zextras.mailbox.util.SoapClient.Request;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.account.accesscontrol.RightManager;
import com.zimbra.cs.mailbox.MailboxManager;
import com.zimbra.cs.mailbox.MailboxTestUtil;
import com.zimbra.cs.mailclient.smtp.SmtpConfig;
import com.zimbra.soap.mail.message.GetMsgRequest;
import com.zimbra.soap.mail.message.SendMsgRequest;
import com.zimbra.soap.mail.type.EmailAddrInfo;
import com.zimbra.soap.mail.type.MsgSpec;
import com.zimbra.soap.mail.type.MsgToSend;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class NotifyHeaderTest extends SoapTestSuite {

	private static GreenMail greenMail;
	private static Provisioning provisioning;
	private static AccountCreator accountCreator;
	private static AccountAction.Factory accountActionFactory;

	@BeforeAll
	public static void setUp() throws Exception {
		MailboxTestUtil.initServer();
		provisioning = Provisioning.getInstance();
		accountCreator = new Factory(provisioning,
				soapExtension.getDefaultDomain()).get();
		accountActionFactory = new AccountAction.Factory(MailboxManager.getInstance(), RightManager.getInstance());

		// Setup SMTP
		greenMail =
				new GreenMail(
						new ServerSetup[]{
								new ServerSetup(
										SmtpConfig.DEFAULT_PORT, SmtpConfig.DEFAULT_HOST, ServerSetup.PROTOCOL_SMTP)
						});
		greenMail.start();
	}

	@Test
	void shouldNotifySubjectChanged_WhenNewMessageAddedToConversation() throws Exception {
		// step1: send message
		final Account account = accountCreator.create();
		final SendMsgRequest request = sendMsgRequest(account);
		final SoapResponse response = callAsAccount(account)
				.setSoapBody(request)
				.call();
		Assertions.assertEquals(200, response.statusCode());


		// step2: reply to conversation
		final SoapResponse replyResponse = callAsAccount(account)
				.setSoapBody(replyToMessage(account, "257")).call();
		Assertions.assertEquals(200, replyResponse.statusCode());

		// step3: GetMsg of reply and see that header contains conversation with updated subject
		final SoapResponse getMsg1Response = callAsAccount(account)
				.setSoapBody(getMsg(account, "257")).call();
		System.out.println("getMsgResponse = " + getMsg1Response.body());
		final SoapResponse getMsgResponse = callAsAccount(account)
				.setSoapBody(getMsg(account, "258")).call();
		System.out.println("getMsgResponse = " + getMsgResponse.body());
		Assertions.assertEquals(200, getMsgResponse.statusCode());
	}

	private Request callAsAccount(Account account) {
		return this.getSoapClient().newRequest()
				.setCaller(account);
	}

	private static SendMsgRequest sendMsgRequest(Account account) throws ServiceException {
		final SendMsgRequest sendMsgRequest = new SendMsgRequest();
		final MsgToSend msgToSend = new MsgToSend();
		msgToSend.setSubject("Test");

		final List<EmailAddrInfo> recipients = new ArrayList<>();
		final EmailAddrInfo from = new EmailAddrInfo(account.getName(), "f");
		final EmailAddrInfo to = new EmailAddrInfo(account.getName(), "t");
		recipients.add(from);
		recipients.add(to);
		msgToSend.setEmailAddresses(recipients);
		msgToSend.setContent("Hello there");
		sendMsgRequest.setMsg(msgToSend);
		return sendMsgRequest;
	}
	private static SendMsgRequest replyToMessage(Account account, String originalMessageId) throws ServiceException {
		final SendMsgRequest sendMsgRequest = new SendMsgRequest();
		final MsgToSend msgToSend = new MsgToSend();
		msgToSend.setSubject("Test");
		msgToSend.setInReplyTo(originalMessageId);

		final List<EmailAddrInfo> recipients = new ArrayList<>();
		final EmailAddrInfo from = new EmailAddrInfo(account.getName(), "f");
		final EmailAddrInfo to = new EmailAddrInfo(account.getName(), "t");
		recipients.add(from);
		recipients.add(to);
		msgToSend.setEmailAddresses(recipients);
		msgToSend.setContent("Hello to you");
		msgToSend.setReplyType("r");
		msgToSend.setOrigId(originalMessageId);
		sendMsgRequest.setMsg(msgToSend);
		return sendMsgRequest;
	}
	private static GetMsgRequest getMsg(Account account, String messageId) throws ServiceException {
		return new GetMsgRequest(new MsgSpec(messageId));
	}


}
