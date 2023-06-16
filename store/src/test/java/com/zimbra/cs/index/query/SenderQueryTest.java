// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SenderQuery}.
 *
 * @author ysasaki
 */
public final class SenderQueryTest {

 @Test
 void comparison() throws Exception {
  assertEquals("<DB[FROM:(>\"test@zimbra.com\") ]>",
    SenderQuery.create(null, ">test@zimbra.com").compile(null, true).toString());
  assertEquals("<DB[FROM:(>=\"test@zimbra.com\") ]>",
    SenderQuery.create(null, ">=test@zimbra.com").compile(null, true).toString());
  assertEquals("<DB[FROM:(<\"test@zimbra.com\") ]>",
    SenderQuery.create(null, "<test@zimbra.com").compile(null, true).toString());
  assertEquals("<DB[FROM:(<=\"test@zimbra.com\") ]>",
    SenderQuery.create(null, "<=test@zimbra.com").compile(null, true).toString());
 }

}
