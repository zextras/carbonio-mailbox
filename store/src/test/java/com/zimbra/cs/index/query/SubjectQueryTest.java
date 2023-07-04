// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.zimbra.cs.index.ZimbraAnalyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import com.zimbra.cs.mailbox.MailboxTestUtil;

/**
 * Unit test for {@link SubjectQuery}.
 *
 * @author ysasaki
 */
public final class SubjectQueryTest {


    @BeforeAll
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

 @Test
 void emptySubject() throws Exception {
  Query query = SubjectQuery.create(ZimbraAnalyzer.getInstance(), "");
  assertEquals(TextQuery.class, query.getClass());
  assertEquals("Q(subject:)", query.toString());
 }

}
