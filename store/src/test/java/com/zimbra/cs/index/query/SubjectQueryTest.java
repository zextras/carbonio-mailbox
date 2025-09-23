// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import static org.junit.jupiter.api.Assertions.*;

import com.zextras.mailbox.MailboxTestSuite;
import com.zimbra.cs.index.ZimbraAnalyzer;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SubjectQuery}.
 *
 * @author ysasaki
 */
public final class SubjectQueryTest extends MailboxTestSuite {


	@Test
	void emptySubject() {
		Query query = SubjectQuery.create(ZimbraAnalyzer.getInstance(), "");
		assertEquals(TextQuery.class, query.getClass());
		assertEquals("Q(subject:)", query.toString());
	}

}
