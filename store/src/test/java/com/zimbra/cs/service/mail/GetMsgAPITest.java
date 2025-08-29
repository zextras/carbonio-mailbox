/*
 * SPDX-FileCopyrightText: 2025 Zextras <https://www.zextras.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 */

package com.zimbra.cs.service.mail;

import com.zextras.mailbox.soap.SoapTestSuite;
import com.zextras.mailbox.util.SoapClient.SoapResponse;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.mailbox.Message;
import com.zimbra.cs.mime.Mime;
import com.zimbra.cs.util.JMSession;
import com.zimbra.soap.mail.message.GetMsgRequest;
import com.zimbra.soap.mail.type.MsgSpec;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("api")
public class GetMsgAPITest extends SoapTestSuite {

	@Test
	void shouldReturnBody() throws Exception {
		final Account account = getCreateAccountFactory().get().create();
		MimeMessage mimeMessage =
				new Mime.FixedMimeMessage(
						JMSession.getSession(), this.getClass().getResourceAsStream("bug-CO-2475.txt"));
		final Message message = getAccountActionFactory().forAccount(account)
				.saveMsgInInbox(mimeMessage);

		final GetMsgRequest getMsgRequest = new GetMsgRequest(new MsgSpec(String.valueOf(message.getId())));

		final SoapResponse soapResponse = getSoapClient().newRequest().setCaller(account)
				.setSoapBody(getMsgRequest)
				.call();
		System.out.println("soapResponse.body() = " + soapResponse.body());
	}
}
