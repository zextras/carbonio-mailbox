// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.service.util;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.common.service.ServiceException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class JWEUtilTest extends MailboxTestSuite {

	@Test
	void testJWE() {
		Map<String, String> map = new HashMap<>();
		String val1 = "jwt";
		String val2 = "encryption";
		map.put("key1", val1);
		map.put("key2", val2);
		try {
			String jwe = JWEUtil.getJWE(map);
			Map<String, String> result = JWEUtil.getDecodedJWE(jwe);
			assertEquals(val1, result.get("key1"));
			assertEquals(val2, result.get("key2"));
		} catch (ServiceException se) {
			fail("testJWE failed");
		}
	}
}
