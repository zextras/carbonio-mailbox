// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link SenderQuery}.
 *
 * @author ysasaki
 */
public final class SenderQueryTest {

    @Test
    public void comparison() throws Exception {
        Assert.assertEquals("<DB[FROM:(>\"test@zimbra.com\") ]>",
                SenderQuery.create(null, ">test@zimbra.com").compile(null, true).toString());
        Assert.assertEquals("<DB[FROM:(>=\"test@zimbra.com\") ]>",
                SenderQuery.create(null, ">=test@zimbra.com").compile(null, true).toString());
        Assert.assertEquals("<DB[FROM:(<\"test@zimbra.com\") ]>",
                SenderQuery.create(null, "<test@zimbra.com").compile(null, true).toString());
        Assert.assertEquals("<DB[FROM:(<=\"test@zimbra.com\") ]>",
                SenderQuery.create(null, "<=test@zimbra.com").compile(null, true).toString());
    }

}
