// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.zimlet;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.account.Account;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ZimletUserProperties}
 *
 * @author ysasaki
 */
public final class ZimletUserPropertiesTest extends MailboxTestSuite {

	@Test
	void save() throws Exception {
		Account account = createAccount().create();
		ZimletUserProperties prop = ZimletUserProperties.getProperties(account);
		prop.setProperty("phone", "123123", "aaaaaaaaaaaa");
		prop.setProperty("phone", "number", "bar");
		prop.saveProperties(account);

		String[] values = account.getZimletUserProperties();
		Arrays.sort(values);
		assertArrayEquals(new String[]{"phone:123123:aaaaaaaaaaaa", "phone:number:bar"}, values);
	}
}
