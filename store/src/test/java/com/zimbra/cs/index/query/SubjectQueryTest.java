// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.zimbra.cs.index.ZimbraAnalyzer;
import com.zimbra.cs.mailbox.MailboxTestUtil;

/**
 * Unit test for {@link SubjectQuery}.
 *
 * @author ysasaki
 */
public final class SubjectQueryTest {


    @BeforeClass
    public static void init() throws Exception {
        MailboxTestUtil.initServer();
    }

    @Test
    public void emptySubject() throws Exception {
        Query query = SubjectQuery.create(ZimbraAnalyzer.getInstance(), "");
        Assert.assertEquals(TextQuery.class, query.getClass());
        Assert.assertEquals("Q(subject:)", query.toString());
    }

}
