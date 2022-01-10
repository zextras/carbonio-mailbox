// SPDX-FileCopyrightText: 2022 Synacor, Inc.
// SPDX-FileCopyrightText: 2022 Zextras <https://www.zextras.com>
//
// SPDX-License-Identifier: GPL-2.0-only

package com.zimbra.cs.index.analysis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.base.Strings;
import com.zimbra.cs.index.ZimbraAnalyzerTest;

/**
 * Unit test for {@link MimeTypeTokenStream}.
 *
 * @author ysasaki
 */
public final class MimeTypeTokenStreamTest {

    @Test
    public void tokenize() throws Exception {
        TokenStream stream = new MimeTypeTokenStream(Arrays.asList("image/jpeg", "text/plain", " text/foo/bar ",
                "aaa bbb ccc ddd/eee fff/ggg/hhh"));
        Assert.assertEquals(Arrays.asList("image/jpeg", "image", "text/plain", "text", "text/foo/bar", "text",
                "aaa bbb ccc ddd/eee fff/ggg/hhh", "aaa bbb ccc ddd", "any"), ZimbraAnalyzerTest.toTokens(stream));
    }

    @Test
    public void limit() throws Exception {
        MimeTypeTokenStream stream = new MimeTypeTokenStream("x");
        Assert.assertEquals(Arrays.asList("none"), ZimbraAnalyzerTest.toTokens(stream));

        stream = new MimeTypeTokenStream(Strings.repeat("x", 257));
        Assert.assertEquals(Arrays.asList("none"), ZimbraAnalyzerTest.toTokens(stream));

        List<String> list = new ArrayList<String>(200);
        for (int i = 0; i < 200; i++) {
            list.add("x/x");
        }
        stream = new MimeTypeTokenStream(list);
        Assert.assertEquals(101, ZimbraAnalyzerTest.toTokens(stream).size());
    }

}
