// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.util;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Domain;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.mailclient.smtp.SmtpTransport;
import com.zimbra.cs.mailclient.smtp.SmtpsTransport;
import java.util.HashMap;
import javax.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link JMSession}.
 *
 * @author ysasaki
 */
public class JMSessionTest extends MailboxTestSuite {

	@Test
	void getTransport() throws Exception {
		assertSame(SmtpTransport.class,
				JMSession.getSession().getTransport("smtp").getClass());
		assertSame(SmtpsTransport.class,
				JMSession.getSession().getTransport("smtps").getClass());

		assertSame(SmtpTransport.class,
				JMSession.getSmtpSession().getTransport("smtp").getClass());
		assertSame(SmtpsTransport.class,
				JMSession.getSmtpSession().getTransport("smtps").getClass());
	}

	@Test
	void messageID() throws Exception {
		Provisioning prov = Provisioning.getInstance();
		Domain domain = prov.createDomain("example.com", new HashMap<String, Object>());
		Account account = prov.createAccount("user1@example.com", "test123",
				new HashMap<String, Object>());

		MimeMessage mm = new MimeMessage(JMSession.getSmtpSession(account));
		mm.saveChanges();
		assertEquals(domain.getName() + '>', mm.getMessageID().split("@")[1],
				"message ID contains account domain");
	}
}
