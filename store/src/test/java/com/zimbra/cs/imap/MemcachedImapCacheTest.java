// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.imap;

import static com.zimbra.cs.service.GetMsgTest.zimbraServerDir;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.memcached.ZimbraMemcachedClient;
import com.zimbra.cs.memcached.MemcachedConnector;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * @author zimbra
 */
public class MemcachedImapCacheTest extends MailboxTestSuite {

	@BeforeAll
	static void setup() {
		final String timezoneFilePath = MemcachedImapCacheTest.class.getResource("/timezones-test.ics").getPath();
		LC.timezone_file.setDefault(timezoneFilePath);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterEach
	public void tearDown() throws Exception {
	}

	@Test
	void testInvalidObject() {
		try {

			mockStatic(MemcachedConnector.class);
			ZimbraMemcachedClient memcachedClient = new MockZimbraMemcachedClient();
			when(MemcachedConnector.getClient()).thenReturn(memcachedClient);
			ImapFolder folder = mock(ImapFolder.class);
			MemcachedImapCache imapCache = new MemcachedImapCache();
			imapCache.put("trash", folder);
			ImapFolder folderDeserz = imapCache.get("trash");
			assertNull(folderDeserz);
		} catch (Exception e) {
			fail("Exception should not be thrown");
		}
	}

	public class MockZimbraMemcachedClient extends ZimbraMemcachedClient {

		@Override
		public Object get(String key) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try {
				if (key.equals("zmImap:trash")) {
					ObjectOutputStream oout = new ObjectOutputStream(bout);
					oout.writeObject(new Hacker("hacked"));
					oout.close();
				}
			} catch (Exception e) {
				return bout.toByteArray();
			}
			return bout.toByteArray();
		}

		@Override
		public boolean put(String key, Object value, boolean waitForAck) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			try (ObjectOutputStream oout = new ObjectOutputStream(bout)) {
				oout.writeObject(new Hacker("hacked"));
			} catch (Exception e) {
				return false;
			}
			return true;
		}
	}
}
