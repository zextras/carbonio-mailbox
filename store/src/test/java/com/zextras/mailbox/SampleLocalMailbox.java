// SPDX-FileCopyrightText: 2024 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: AGPL-3.0-only

package com.zextras.mailbox;

import com.zextras.mailbox.util.InMemoryLdapServer;
import com.zimbra.cs.db.HSQLDB;
import org.eclipse.jetty.server.Server;

/**
 * A Mailbox that can be used for testing. You can spin it up and interact with APIs, except it uses
 * an in memory db {@link HSQLDB} and an in memory LDAP {@link InMemoryLdapServer}.
 * <p>
 * Please run this class with java.library.path pointing to native module target (native/target) so
 * native library is loaded
 */
public class SampleLocalMailbox {

	public static void main(String[] args) throws Exception {
		final Mailbox server = new MailboxSetupHelper("./store",
				"store/src/test/resources/timezones-test.ics").create();
		server.start(false);
	}

}
