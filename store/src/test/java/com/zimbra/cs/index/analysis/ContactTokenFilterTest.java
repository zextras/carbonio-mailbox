// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.io.StringReader;
import java.util.Collections;

import org.apache.lucene.analysis.TokenFilter;
import org.junit.Assert;
import org.junit.Test;

import com.zimbra.cs.index.ZimbraAnalyzerTest;

/**
 * Unit test for {@link ContactTokenFilter}.
 *
 * @author ysasaki
 */
public class ContactTokenFilterTest {

    @Test
    public void contactDataFilter() throws Exception {
        AddrCharTokenizer tokenizer = new AddrCharTokenizer(new StringReader("all-snv"));
        TokenFilter filter = new ContactTokenFilter(tokenizer);
        Assert.assertEquals(Collections.singletonList("all-snv"),
                ZimbraAnalyzerTest.toTokens(filter));

        tokenizer.reset(new StringReader("."));
        Assert.assertEquals(Collections.EMPTY_LIST,
                ZimbraAnalyzerTest.toTokens(filter));

        tokenizer.reset(new StringReader(".. ."));
        Assert.assertEquals(Collections.singletonList(".."),
                ZimbraAnalyzerTest.toTokens(filter));

        tokenizer.reset(new StringReader(".abc"));
        Assert.assertEquals(Collections.singletonList(".abc"),
                ZimbraAnalyzerTest.toTokens(filter));

        tokenizer.reset(new StringReader("a"));
        Assert.assertEquals(Collections.singletonList("a"),
                ZimbraAnalyzerTest.toTokens(filter));

        tokenizer.reset(new StringReader("test.com"));
        Assert.assertEquals(Collections.singletonList("test.com"),
                ZimbraAnalyzerTest.toTokens(filter));

        tokenizer.reset(new StringReader("user1@zim"));
        Assert.assertEquals(Collections.singletonList("user1@zim"),
                ZimbraAnalyzerTest.toTokens(filter));

        tokenizer.reset(new StringReader("user1@zimbra.com"));
        Assert.assertEquals(Collections.singletonList("user1@zimbra.com"),
                ZimbraAnalyzerTest.toTokens(filter));
    }

}
